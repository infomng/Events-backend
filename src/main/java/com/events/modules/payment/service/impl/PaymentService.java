package com.events.modules.payment.service.impl;

import com.events.common.exception.BadRequestException;
import com.events.common.i18n.LocalizationService;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.booking.dto.BookingPaymentDto;
import com.events.modules.booking.service.IBookingService;
import com.events.modules.payment.dto.*;
import com.events.modules.payment.dto.mapper.IPaymentMapper;
import com.events.modules.payment.entity.Payment;
import com.events.modules.payment.enumeration.PaymentGatewayEnum;
import com.events.modules.payment.enumeration.PaymentStatusEnum;
import com.events.modules.payment.exception.InvalidPaymentStatusException;
import com.events.modules.payment.exception.PaymentAlreadyProcessedException;
import com.events.modules.payment.exception.PaymentFailedException;
import com.events.modules.payment.exception.PaymentNotFoundException;
import com.events.modules.payment.repository.IPaymentRepository;
import com.events.modules.payment.service.IPaymentService;
import com.events.modules.payment.service.StripeGatewayService;
import com.events.modules.user.entity.User;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Les échecs de paiement doivent être enregistrés en base : on ne rollback pas sur ces exceptions métier.
@Transactional(noRollbackFor = {PaymentFailedException.class, InvalidPaymentStatusException.class})
@Slf4j
public class PaymentService implements IPaymentService {

    /** Statuts qui bloquent la création d'un nouveau paiement pour la même réservation. */
    private static final Set<PaymentStatusEnum> ACTIVE_STATUSES =
            EnumSet.of(PaymentStatusEnum.PENDING, PaymentStatusEnum.PROCESSING, PaymentStatusEnum.COMPLETED);

    private static final Set<String> HANDLED_WEBHOOK_EVENTS = Set.of(
            "payment_intent.succeeded",
            "payment_intent.processing",
            "payment_intent.payment_failed",
            "payment_intent.canceled"
    );

    private final IPaymentRepository paymentRepository;
    private final IAuthService authService;
    private final IBookingService bookingService;
    private final IPaymentMapper paymentMapper;
    private final LocalizationService localizationService;
    private final StripeGatewayService stripeGatewayService;

    @Override
    public PaymentResponseDto initiatePayment(InitiatePaymentDto dto) {
        User currentUser = authService.getCurrentUser();

        // Seul Stripe est réellement intégré : les autres passerelles permettraient de "payer" sans contrôle.
        if (dto.paymentGateway() != PaymentGatewayEnum.STRIPE) {
            throw new PaymentFailedException(
                    localizationService.getMessage("payment.gateway.unsupported", dto.paymentGateway())
            );
        }

        // Le montant vient de la réservation en base, jamais du client. Vérifie aussi la propriété.
        BookingPaymentDto booking = bookingService.getPayableBooking(dto.bookingId(), currentUser.getId());

        if (paymentRepository.existsByBookingIdAndStatusIn(booking.id(), ACTIVE_STATUSES)) {
            throw new PaymentAlreadyProcessedException();
        }

        String currency = stripeGatewayService.getCurrency();
        Payment payment = Payment.builder()
                .userId(currentUser.getId())
                .bookingId(booking.id())
                .amount(booking.totalAmount())
                .currency(currency.toUpperCase())
                .paymentMethod(dto.paymentMethod())
                .paymentGateway(dto.paymentGateway())
                .metadata(dto.metadata())
                .status(PaymentStatusEnum.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        log.info("Payment initiated: {} for booking: {} by user: {}",
                payment.getPaymentReference(), booking.id(), currentUser.getId());

        try {
            PaymentIntent paymentIntent = stripeGatewayService.createPaymentIntent(
                    toMinorUnits(payment.getAmount()),
                    currency,
                    "Payment for booking " + booking.bookingReference(),
                    payment.getPaymentReference()
            );
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setStripeClientSecret(paymentIntent.getClientSecret());
            paymentRepository.save(payment);

            return new PaymentResponseDto(
                    payment.getId(),
                    payment.getPaymentReference(),
                    payment.getStatus(),
                    null,
                    paymentIntent.getClientSecret(),
                    localizationService.getMessage("payment.stripe.initiated")
            );
        } catch (StripeException e) {
            log.error("Failed to create Stripe Payment Intent for payment {}: {}", payment.getId(), e.getMessage());
            payment.markAsFailed(e.getMessage());
            paymentRepository.save(payment);
            throw new PaymentFailedException(localizationService.getMessage("payment.stripe.failed"));
        }
    }

    /**
     * Synchronise le paiement avec Stripe, à la demande du client (après le retour de Stripe.js).
     * Le statut est lu chez Stripe : le client ne peut pas déclarer un paiement réussi.
     * La source de vérité reste le webhook ({@link #handleStripeWebhook}).
     */
    @Override
    public PaymentStatusEnum confirmPayment(ConfirmPaymentDto dto) {
        User currentUser = authService.getCurrentUser();

        Payment payment = paymentRepository.findForUpdateByPaymentReference(dto.paymentReference())
                .filter(p -> p.getUserId().equals(currentUser.getId()))
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.reference", dto.paymentReference())
                ));

        if (payment.getStatus() == PaymentStatusEnum.COMPLETED) {
            return payment.getStatus(); // idempotent : déjà confirmé (par exemple par le webhook)
        }

        if (payment.isFinal() || payment.getPaymentGateway() != PaymentGatewayEnum.STRIPE
                || payment.getStripePaymentIntentId() == null) {
            throw new InvalidPaymentStatusException();
        }

        try {
            PaymentIntent paymentIntent = stripeGatewayService.retrievePaymentIntent(payment.getStripePaymentIntentId());
            applyPaymentIntent(payment, paymentIntent);
        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe Payment Intent {}: {}", payment.getStripePaymentIntentId(), e.getMessage());
            throw new PaymentFailedException(localizationService.getMessage("payment.stripe.failed.api.error", e.getMessage()));
        }

        return payment.getStatus();
    }

    @Override
    public void handleStripeWebhook(String payload, String signatureHeader) {
        Event event;
        try {
            event = stripeGatewayService.constructWebhookEvent(payload, signatureHeader);
        } catch (SignatureVerificationException e) {
            log.warn("Rejected Stripe webhook with invalid signature");
            throw new BadRequestException("Invalid Stripe signature");
        }

        if (!HANDLED_WEBHOOK_EVENTS.contains(event.getType())) {
            log.debug("Ignoring Stripe webhook event {} ({})", event.getId(), event.getType());
            return;
        }

        PaymentIntent paymentIntent = extractPaymentIntent(event);

        paymentRepository.findForUpdateByStripePaymentIntentId(paymentIntent.getId())
                .ifPresentOrElse(
                        payment -> {
                            if (payment.isFinal()) {
                                log.info("Stripe webhook {} ignored: payment {} already {}",
                                        event.getId(), payment.getPaymentReference(), payment.getStatus());
                                return;
                            }
                            applyPaymentIntent(payment, paymentIntent);
                        },
                        () -> log.warn("Stripe webhook {} references unknown PaymentIntent {}",
                                event.getId(), paymentIntent.getId())
                );
    }

    @Override
    @Transactional(readOnly = true)
    public GetPaymentDto getPaymentById(UUID paymentId) {
        User currentUser = authService.getCurrentUser();

        Payment payment = paymentRepository.findByUserIdAndPaymentId(currentUser.getId(), paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.id", paymentId)
                ));

        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public GetPaymentDto getPaymentByReference(String paymentReference) {
        return paymentMapper.toDto(getOwnedPaymentByReference(paymentReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetPaymentDto> getMyPayments(int page, int size) {
        User currentUser = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Payment> payments = paymentRepository.findByUserId(currentUser.getId(), pageable);

        return payments.map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetPaymentDto> getMyPaymentsByStatus(PaymentStatusEnum status) {
        User currentUser = authService.getCurrentUser();

        List<Payment> payments = paymentRepository.findByUserIdAndStatus(currentUser.getId(), status);

        return paymentMapper.toDtoList(payments);
    }

    @Override
    public void cancelPayment(UUID paymentId) {
        User currentUser = authService.getCurrentUser();

        Payment payment = paymentRepository.findForUpdateById(paymentId)
                .filter(p -> p.getUserId().equals(currentUser.getId()))
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.id", paymentId)
                ));

        // Can only cancel pending or processing payments
        if (payment.isFinal()) {
            throw new InvalidPaymentStatusException();
        }

        // Annuler d'abord chez Stripe : si l'intention a déjà abouti, Stripe refuse et on ne marque rien.
        if (payment.getStripePaymentIntentId() != null) {
            try {
                stripeGatewayService.cancelPaymentIntent(payment.getStripePaymentIntentId());
            } catch (StripeException e) {
                log.warn("Stripe refused to cancel PaymentIntent {}: {}", payment.getStripePaymentIntentId(), e.getMessage());
                throw new PaymentFailedException(localizationService.getMessage("payment.stripe.cancel.failed", e.getMessage()));
            }
        }

        payment.markAsCancelled("Cancelled by user");
        paymentRepository.save(payment);

        log.info("Payment cancelled: {} by user: {}", payment.getPaymentReference(), currentUser.getId());
    }

    /**
     * Rembourse un paiement. Réservé aux administrateurs (voir le contrôleur).
     */
    @Override
    public void refundPayment(RefundPaymentDto dto) {
        Payment payment = paymentRepository.findForUpdateById(dto.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.id", dto.paymentId())
                ));

        if (payment.getStatus() != PaymentStatusEnum.COMPLETED) {
            throw new InvalidPaymentStatusException(payment.getStatus(), PaymentStatusEnum.COMPLETED);
        }

        if (payment.getPaymentGateway() != PaymentGatewayEnum.STRIPE || payment.getStripePaymentIntentId() == null) {
            throw new InvalidPaymentStatusException();
        }

        // Remboursement réel chez Stripe avant de changer l'état local.
        try {
            stripeGatewayService.refundPaymentIntent(payment.getStripePaymentIntentId(), "refund-" + payment.getId());
        } catch (StripeException e) {
            log.error("Stripe refund failed for payment {}: {}", payment.getPaymentReference(), e.getMessage());
            throw new PaymentFailedException(localizationService.getMessage("payment.stripe.refund.failed", e.getMessage()));
        }

        payment.markAsRefunded(dto.reason());
        paymentRepository.save(payment);
        bookingService.cancelBookingAfterRefund(payment.getBookingId());

        log.info("Payment refunded: {} - Reason: {}", payment.getPaymentReference(), dto.reason());

        // TODO: Cancel tickets
        // TODO: Send refund confirmation email
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusEnum checkPaymentStatus(String paymentReference) {
        return getOwnedPaymentByReference(paymentReference).getStatus();
    }

    /**
     * Applique l'état d'un PaymentIntent Stripe au paiement local.
     * Vérifie que l'intention correspond bien au paiement (identifiant, montant, devise).
     */
    private void applyPaymentIntent(Payment payment, PaymentIntent paymentIntent) {
        if (!paymentIntent.getId().equals(payment.getStripePaymentIntentId())) {
            throw new PaymentFailedException(localizationService.getMessage("payment.stripe.mismatch"));
        }

        boolean amountMatches = paymentIntent.getAmount() != null
                && paymentIntent.getAmount() == toMinorUnits(payment.getAmount());
        boolean currencyMatches = payment.getCurrency().equalsIgnoreCase(paymentIntent.getCurrency());
        if (!amountMatches || !currencyMatches) {
            log.error("Stripe PaymentIntent {} does not match payment {} (amount {} {} vs {} {})",
                    paymentIntent.getId(), payment.getPaymentReference(),
                    paymentIntent.getAmount(), paymentIntent.getCurrency(),
                    payment.getAmount(), payment.getCurrency());
            payment.markAsFailed("Amount or currency mismatch with Stripe");
            paymentRepository.save(payment);
            throw new PaymentFailedException(localizationService.getMessage("payment.amount.mismatch"));
        }

        String stripeStatus = paymentIntent.getStatus();
        switch (stripeStatus) {
            case "succeeded" -> {
                payment.markAsPaid(paymentIntent.getId());
                payment.setFailureReason(null);
                bookingService.confirmBookingPayment(payment.getBookingId(), payment.getId().toString());
                log.info("Payment completed: {}", payment.getPaymentReference());
                // TODO: Trigger ticket generation
                // TODO: Send confirmation email
            }
            case "processing" -> payment.markAsProcessing();
            case "canceled" -> payment.markAsCancelled(
                    "Stripe PaymentIntent canceled: " + paymentIntent.getCancellationReason());
            // requires_payment_method / requires_confirmation / requires_action / requires_capture :
            // le paiement n'est pas encore abouti, il reste en attente côté client.
            default -> {
                if (paymentIntent.getLastPaymentError() != null) {
                    payment.setFailureReason(paymentIntent.getLastPaymentError().getMessage());
                }
            }
        }

        payment.setGatewayResponse("stripe_status=" + stripeStatus);
        paymentRepository.save(payment);
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        StripeObject object = event.getDataObjectDeserializer().getObject().orElse(null);
        if (object == null) {
            // Version d'API différente de celle du SDK : la signature a été vérifiée, on désérialise quand même.
            try {
                object = event.getDataObjectDeserializer().deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                log.error("Unable to deserialize Stripe webhook {}: {}", event.getId(), e.getMessage());
                throw new BadRequestException("Unreadable Stripe event");
            }
        }
        if (!(object instanceof PaymentIntent paymentIntent)) {
            throw new BadRequestException("Unexpected Stripe event payload");
        }
        return paymentIntent;
    }

    private Payment getOwnedPaymentByReference(String paymentReference) {
        User currentUser = authService.getCurrentUser();

        // Même erreur si absent ou appartenant à un autre utilisateur : pas d'énumération possible.
        return paymentRepository.findByPaymentReference(paymentReference)
                .filter(p -> p.getUserId().equals(currentUser.getId()))
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.reference", paymentReference)
                ));
    }

    /** Convertit un montant en plus petite unité monétaire (centimes). */
    private static long toMinorUnits(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
    }
}
