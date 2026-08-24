package com.events.modules.payment.service.impl;

import com.events.common.i18n.LocalizationService;
import com.events.modules.auth.service.auth.IAuthService;
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
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final IAuthService authService;
    private final IPaymentMapper paymentMapper;
    private final LocalizationService localizationService;
    private final StripeGatewayService stripeGatewayService; // Injected StripeGatewayService

    @Override
    public PaymentResponseDto initiatePayment(InitiatePaymentDto dto) {
        User currentUser = authService.getCurrentUser();

        // Check if payment already exists for this booking
        if (paymentRepository.existsByBookingId(dto.bookingId())) {
            throw new PaymentAlreadyProcessedException();
        }

        // Create payment entity
        Payment payment = paymentMapper.toEntity(dto);
        payment.setUserId(currentUser.getId());
        payment.setStatus(PaymentStatusEnum.PENDING);

        // Save payment
        payment = paymentRepository.save(payment);

        log.info("Payment initiated: {} for booking: {} by user: {}",
                payment.getPaymentReference(), dto.bookingId(), currentUser.getId());

        String clientSecret = null;
        String redirectUrl = null;
        String message = localizationService.getMessage("payment.redirect.message");

        if (payment.getPaymentGateway() == PaymentGatewayEnum.STRIPE) {
            try {
                // Stripe requires amount in the smallest currency unit (e.g., cents)
                Long amountInCents = payment.getAmount().multiply(new BigDecimal("100")).longValueExact();
                PaymentIntent paymentIntent = stripeGatewayService.createPaymentIntent(
                        amountInCents,
                        payment.getCurrency(),
                        "Payment for booking " + payment.getBookingId(),
                        payment.getPaymentReference()
                );
                payment.setStripePaymentIntentId(paymentIntent.getId());
                payment.setStripeClientSecret(paymentIntent.getClientSecret());
                paymentRepository.save(payment); // Save updated payment with Stripe details
                clientSecret = paymentIntent.getClientSecret();
                message = localizationService.getMessage("payment.stripe.initiated");
            } catch (StripeException e) {
                log.error("Failed to create Stripe Payment Intent for payment {}: {}", payment.getId(), e.getMessage());
                payment.markAsFailed(e.getMessage());
                paymentRepository.save(payment);
                throw new PaymentFailedException(localizationService.getMessage("payment.stripe.failed"));
            }
        } else {
            // For other payment gateways or mock implementation
            redirectUrl = generatePaymentRedirectUrl(payment, dto.returnUrl());
        }

        return new PaymentResponseDto(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getStatus(),
                redirectUrl,
                clientSecret,
                message
        );
    }

    @Override
    public void confirmPayment(ConfirmPaymentDto dto) {
        Payment payment = paymentRepository.findByPaymentReference(dto.paymentReference())
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.reference", dto.paymentReference())
                ));

        // Validate payment status
        if (payment.getStatus() == PaymentStatusEnum.COMPLETED) {
            throw new PaymentAlreadyProcessedException();
        }

        if (payment.getStatus() == PaymentStatusEnum.CANCELLED) {
            throw new InvalidPaymentStatusException();
        }

        if (payment.getPaymentGateway() == PaymentGatewayEnum.STRIPE) {
            try {
                PaymentIntent paymentIntent = stripeGatewayService.retrievePaymentIntent(payment.getStripePaymentIntentId());
                String stripeStatus = paymentIntent.getStatus();

                switch (stripeStatus) {
                    case "succeeded":
                    case "processing":
                    case "requires_capture": // If configured for manual capture
                        payment.markAsPaid(paymentIntent.getId());
                        if (paymentIntent.getLastPaymentError() != null) {
                            payment.setGatewayResponse(paymentIntent.getLastPaymentError().getMessage());
                        }
                        break;
                    case "requires_action": // Payment requires customer action (e.g., 3D Secure)
                    case "requires_confirmation":
                        log.warn("Stripe Payment Intent {} requires action or confirmation. Attempting to confirm.", paymentIntent.getId());
                        PaymentIntent confirmedPaymentIntent = stripeGatewayService.confirmPaymentIntent(paymentIntent.getId());
                        stripeStatus = confirmedPaymentIntent.getStatus(); // Re-check status after confirmation attempt

                        if ("succeeded".equals(stripeStatus) || "processing".equals(stripeStatus) || "requires_capture".equals(stripeStatus)) {
                            payment.markAsPaid(confirmedPaymentIntent.getId());
                            if (confirmedPaymentIntent.getLastPaymentError() != null) {
                                payment.setGatewayResponse(confirmedPaymentIntent.getLastPaymentError().getMessage());
                            }
                        } else {
                            payment.markAsFailed("Stripe payment requires further action or failed confirmation. Current status: " + stripeStatus);
                            paymentRepository.save(payment);
                            throw new PaymentFailedException(localizationService.getMessage("payment.stripe.failed.confirmation", stripeStatus));
                        }
                        break;
                    case "canceled":
                        payment.markAsFailed("Stripe payment was cancelled. Status: " + stripeStatus);
                        paymentRepository.save(payment);
                        throw new InvalidPaymentStatusException();
                    default:
                        payment.markAsFailed("Unexpected Stripe payment status: " + stripeStatus);
                        paymentRepository.save(payment);
                        throw new PaymentFailedException(localizationService.getMessage("payment.stripe.unexpected.status", stripeStatus));
                }
            } catch (StripeException e) {
                log.error("Failed to confirm Stripe Payment Intent {}: {}", payment.getStripePaymentIntentId(), e.getMessage());
                payment.markAsFailed(e.getMessage());
                paymentRepository.save(payment);
                throw new PaymentFailedException(localizationService.getMessage("payment.stripe.failed.api.error", e.getMessage()));
            }
        } else {
            // Existing logic for other gateways
            payment.markAsPaid(dto.transactionId());
        }

        if (dto.gatewayResponse() != null) {
            payment.setGatewayResponse(dto.gatewayResponse());
        }

        paymentRepository.save(payment);

        log.info("Payment confirmed: {} with transaction: {}",
                dto.paymentReference(), payment.getTransactionId() != null ? payment.getTransactionId() : "N/A");

        // TODO: Update booking status to CONFIRMED
        // TODO: Trigger ticket generation
        // TODO: Send confirmation email
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
        User currentUser = authService.getCurrentUser();

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.reference", paymentReference)
                ));

        // Verify ownership
        if (!payment.getUserId().equals(currentUser.getId())) {
            throw new PaymentNotFoundException(
                    localizationService.getMessage("payment.unauthorized")
            );
        }

        return paymentMapper.toDto(payment);
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

        Payment payment = paymentRepository.findByUserIdAndPaymentId(currentUser.getId(), paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.id", paymentId)
                ));

        // Can only cancel pending or processing payments
        if (payment.getStatus() != PaymentStatusEnum.PENDING &&
            payment.getStatus() != PaymentStatusEnum.PROCESSING) {
            throw new InvalidPaymentStatusException();
        }

        payment.setStatus(PaymentStatusEnum.CANCELLED);
        paymentRepository.save(payment);

        log.info("Payment cancelled: {} by user: {}", payment.getPaymentReference(), currentUser.getId());
    }

    @Override
    public void refundPayment(RefundPaymentDto dto) {
        Payment payment = paymentRepository.findById(dto.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.id", dto.paymentId())
                ));

        // Can only refund completed payments
        if (payment.getStatus() != PaymentStatusEnum.COMPLETED) {
            throw new InvalidPaymentStatusException();
        }

        // Mark as refunded
        payment.markAsRefunded();
        payment.setFailureReason(dto.reason());
        paymentRepository.save(payment);

        log.info("Payment refunded: {} - Reason: {}", payment.getPaymentReference(), dto.reason());

        // TODO: Integrate with payment gateway for actual refund
        // TODO: Update booking status
        // TODO: Cancel tickets
        // TODO: Send refund confirmation email
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusEnum checkPaymentStatus(String paymentReference) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException(
                        localizationService.getMessage("payment.not.found.reference", paymentReference)
                ));

        return payment.getStatus();
    }

    /**
     * Generate payment redirect URL for gateway
     * In real implementation, this would call the payment gateway API
     */
    private String generatePaymentRedirectUrl(Payment payment, String returnUrl) {
        // Mock implementation
        String baseUrl = returnUrl != null ? returnUrl : "http://localhost:3000/payment";
        return String.format("%s/confirm?ref=%s&gateway=%s",
                baseUrl,
                payment.getPaymentReference(),
                payment.getPaymentGateway().name().toLowerCase()
        );
    }
}
