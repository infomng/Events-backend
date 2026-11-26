package com.events.modules.payment.service;

import com.events.common.exception.BadRequestException;
import com.events.common.i18n.LocalizationService;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.booking.dto.BookingPaymentDto;
import com.events.modules.booking.events.BookingReleasedEvent;
import com.events.modules.booking.service.IBookingService;
import com.events.modules.event.enumeration.BookingStatusEnum;
import com.events.modules.payment.dto.ConfirmPaymentDto;
import com.events.modules.payment.dto.InitiatePaymentDto;
import com.events.modules.payment.dto.RefundPaymentDto;
import com.events.modules.payment.dto.mapper.IPaymentMapper;
import com.events.modules.payment.entity.Payment;
import com.events.modules.payment.enumeration.PaymentGatewayEnum;
import com.events.modules.payment.enumeration.PaymentMethodEnum;
import com.events.modules.payment.enumeration.PaymentStatusEnum;
import com.events.modules.payment.exception.PaymentFailedException;
import com.events.modules.payment.exception.PaymentNotFoundException;
import com.events.modules.payment.repository.IPaymentRepository;
import com.events.modules.payment.service.impl.PaymentService;
import com.events.modules.user.entity.User;
import com.stripe.exception.ApiException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock private IPaymentRepository paymentRepository;
    @Mock private IAuthService authService;
    @Mock private IBookingService bookingService;
    @Mock private IPaymentMapper paymentMapper;
    @Mock private LocalizationService localizationService;
    @Mock private StripeGatewayService stripeGatewayService;

    @InjectMocks private PaymentService paymentService;

    private User currentUser;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(UUID.randomUUID()).build();
        bookingId = UUID.randomUUID();
        given(authService.getCurrentUser()).willReturn(currentUser);
        given(localizationService.getMessage(anyString(), any(Object[].class))).willReturn("message");
        given(localizationService.getMessage(anyString())).willReturn("message");
        given(stripeGatewayService.getCurrency()).willReturn("usd");
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));
        given(bookingService.confirmBookingPayment(any(), any())).willReturn(true);
    }

    private Payment stripePayment(UUID ownerId, PaymentStatusEnum status) {
        return Payment.builder()
                .id(UUID.randomUUID())
                .userId(ownerId)
                .bookingId(bookingId)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .status(status)
                .paymentMethod(PaymentMethodEnum.values()[0])
                .paymentGateway(PaymentGatewayEnum.STRIPE)
                .paymentReference("PAY-TEST")
                .stripePaymentIntentId("pi_123")
                .build();
    }

    private static PaymentIntent intent(String status, long amount, String currency) {
        PaymentIntent pi = new PaymentIntent();
        pi.setId("pi_123");
        pi.setStatus(status);
        pi.setAmount(amount);
        pi.setCurrency(currency);
        pi.setClientSecret("secret");
        return pi;
    }

    @Nested
    class InitiatePaymentTest {

        @Test
        void amountComesFromBookingNotFromClient() throws Exception {
            given(bookingService.getPayableBooking(bookingId, currentUser.getId()))
                    .willReturn(new BookingPaymentDto(bookingId, currentUser.getId(),
                            new BigDecimal("120.50"), BookingStatusEnum.PENDING, "BK-1"));
            given(stripeGatewayService.createPaymentIntent(anyLong(), anyString(), anyString(), any()))
                    .willReturn(intent("requires_payment_method", 12050, "usd"));

            paymentService.initiatePayment(new InitiatePaymentDto(
                    bookingId, PaymentMethodEnum.values()[0], PaymentGatewayEnum.STRIPE, null));

            verify(stripeGatewayService).createPaymentIntent(eq(12050L), eq("usd"), anyString(), any());
        }

        @Test
        void rejectsNonStripeGateway() {
            assertThatThrownBy(() -> paymentService.initiatePayment(new InitiatePaymentDto(
                    bookingId, PaymentMethodEnum.values()[0], PaymentGatewayEnum.INTERNAL, null)))
                    .isInstanceOf(PaymentFailedException.class);

            verify(paymentRepository, never()).save(any());
        }
    }

    @Nested
    class ConfirmPaymentTest {

        @Test
        void cannotConfirmSomeoneElsesPayment() {
            Payment payment = stripePayment(UUID.randomUUID(), PaymentStatusEnum.PENDING);
            given(paymentRepository.findForUpdateByPaymentReference("PAY-TEST")).willReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.confirmPayment(new ConfirmPaymentDto("PAY-TEST")))
                    .isInstanceOf(PaymentNotFoundException.class);
        }

        @Test
        void succeededIntentCompletesPaymentAndConfirmsBooking() throws Exception {
            Payment payment = stripePayment(currentUser.getId(), PaymentStatusEnum.PENDING);
            given(paymentRepository.findForUpdateByPaymentReference("PAY-TEST")).willReturn(Optional.of(payment));
            given(stripeGatewayService.retrievePaymentIntent("pi_123")).willReturn(intent("succeeded", 5000, "usd"));

            PaymentStatusEnum status = paymentService.confirmPayment(new ConfirmPaymentDto("PAY-TEST"));

            assertThat(status).isEqualTo(PaymentStatusEnum.COMPLETED);
            verify(bookingService).confirmBookingPayment(bookingId, payment.getId().toString());
        }

        @Test
        void paymentForBookingThatCannotBeHonouredIsRefunded() throws Exception {
            Payment payment = stripePayment(currentUser.getId(), PaymentStatusEnum.PENDING);
            given(paymentRepository.findForUpdateByPaymentReference("PAY-TEST")).willReturn(Optional.of(payment));
            given(stripeGatewayService.retrievePaymentIntent("pi_123")).willReturn(intent("succeeded", 5000, "usd"));
            given(bookingService.confirmBookingPayment(any(), any())).willReturn(false);

            PaymentStatusEnum status = paymentService.confirmPayment(new ConfirmPaymentDto("PAY-TEST"));

            assertThat(status).isEqualTo(PaymentStatusEnum.REFUNDED);
            verify(stripeGatewayService).refundPaymentIntent("pi_123", "refund-" + payment.getId());
        }

        @Test
        void processingIntentIsNotTreatedAsPaid() throws Exception {
            Payment payment = stripePayment(currentUser.getId(), PaymentStatusEnum.PENDING);
            given(paymentRepository.findForUpdateByPaymentReference("PAY-TEST")).willReturn(Optional.of(payment));
            given(stripeGatewayService.retrievePaymentIntent("pi_123")).willReturn(intent("processing", 5000, "usd"));

            PaymentStatusEnum status = paymentService.confirmPayment(new ConfirmPaymentDto("PAY-TEST"));

            assertThat(status).isEqualTo(PaymentStatusEnum.PROCESSING);
            verify(bookingService, never()).confirmBookingPayment(any(), any());
        }

        @Test
        void amountMismatchFailsPayment() throws Exception {
            Payment payment = stripePayment(currentUser.getId(), PaymentStatusEnum.PENDING);
            given(paymentRepository.findForUpdateByPaymentReference("PAY-TEST")).willReturn(Optional.of(payment));
            given(stripeGatewayService.retrievePaymentIntent("pi_123")).willReturn(intent("succeeded", 1, "usd"));

            assertThatThrownBy(() -> paymentService.confirmPayment(new ConfirmPaymentDto("PAY-TEST")))
                    .isInstanceOf(PaymentFailedException.class);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatusEnum.FAILED);
            verify(bookingService, never()).confirmBookingPayment(any(), any());
        }
    }

    @Nested
    class OwnershipRefundAndWebhookPaymentTest {

        @Test
        void checkStatusOfSomeoneElsesPaymentIsNotFound() {
            given(paymentRepository.findByPaymentReference("PAY-TEST"))
                    .willReturn(Optional.of(stripePayment(UUID.randomUUID(), PaymentStatusEnum.COMPLETED)));

            assertThatThrownBy(() -> paymentService.checkPaymentStatus("PAY-TEST"))
                    .isInstanceOf(PaymentNotFoundException.class);
        }

        @Test
        void refundCallsStripeBeforeMarkingRefunded() throws Exception {
            Payment payment = stripePayment(UUID.randomUUID(), PaymentStatusEnum.COMPLETED);
            given(paymentRepository.findForUpdateById(payment.getId())).willReturn(Optional.of(payment));

            paymentService.refundPayment(new RefundPaymentDto(payment.getId(), "customer request"));

            verify(stripeGatewayService).refundPaymentIntent("pi_123", "refund-" + payment.getId());
            assertThat(payment.getStatus()).isEqualTo(PaymentStatusEnum.REFUNDED);
            verify(bookingService).cancelBookingAfterRefund(bookingId);
        }

        @Test
        void refundFailureAtStripeKeepsPaymentCompleted() throws Exception {
            Payment payment = stripePayment(UUID.randomUUID(), PaymentStatusEnum.COMPLETED);
            given(paymentRepository.findForUpdateById(payment.getId())).willReturn(Optional.of(payment));
            given(stripeGatewayService.refundPaymentIntent(anyString(), anyString()))
                    .willThrow(new ApiException("boom", null, null, 500, null));

            assertThatThrownBy(() -> paymentService.refundPayment(new RefundPaymentDto(payment.getId(), "x")))
                    .isInstanceOf(PaymentFailedException.class);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatusEnum.COMPLETED);
        }

        @Test
        void cancelRefusedByStripeKeepsPaymentPending() throws Exception {
            Payment payment = stripePayment(currentUser.getId(), PaymentStatusEnum.PENDING);
            given(paymentRepository.findForUpdateById(payment.getId())).willReturn(Optional.of(payment));
            given(stripeGatewayService.cancelPaymentIntent("pi_123"))
                    .willThrow(new ApiException("already succeeded", null, null, 400, null));

            assertThatThrownBy(() -> paymentService.cancelPayment(payment.getId()))
                    .isInstanceOf(PaymentFailedException.class);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatusEnum.PENDING);
        }

        @Test
        void releasedBookingCancelsPendingPayment() throws Exception {
            Payment payment = stripePayment(currentUser.getId(), PaymentStatusEnum.PENDING);
            given(paymentRepository.findAllByBookingId(bookingId)).willReturn(List.of(payment));

            paymentService.onBookingReleased(new BookingReleasedEvent(bookingId));

            verify(stripeGatewayService).cancelPaymentIntent("pi_123");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatusEnum.CANCELLED);
        }

        @Test
        void cancelledEventRefundsPaidBookingAndCancelsIt() throws Exception {
            Payment payment = stripePayment(currentUser.getId(), PaymentStatusEnum.COMPLETED);
            given(paymentRepository.findAllByBookingId(bookingId)).willReturn(List.of(payment));

            boolean refunded = paymentService.refundBookingForCancelledEvent(bookingId);

            assertThat(refunded).isTrue();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatusEnum.REFUNDED);
            verify(bookingService).cancelBookingAfterRefund(bookingId);
        }

        @Test
        void failedRefundIsFlaggedButBookingStillCancelled() throws Exception {
            Payment payment = stripePayment(currentUser.getId(), PaymentStatusEnum.COMPLETED);
            given(paymentRepository.findAllByBookingId(bookingId)).willReturn(List.of(payment));
            given(stripeGatewayService.refundPaymentIntent(anyString(), anyString()))
                    .willThrow(new ApiException("stripe down", null, null, 503, null));

            boolean refunded = paymentService.refundBookingForCancelledEvent(bookingId);

            assertThat(refunded).isFalse();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatusEnum.COMPLETED);
            assertThat(payment.getFailureReason()).startsWith("REFUND_REQUIRED");
            verify(bookingService).cancelBookingAfterRefund(bookingId);
        }

        @Test
        void webhookWithInvalidSignatureIsRejected() throws Exception {
            given(stripeGatewayService.constructWebhookEvent(anyString(), anyString()))
                    .willThrow(new SignatureVerificationException("bad", "sig"));

            assertThatThrownBy(() -> paymentService.handleStripeWebhook("{}", "sig"))
                    .isInstanceOf(BadRequestException.class);

            verify(paymentRepository, never()).findForUpdateByStripePaymentIntentId(any());
        }
    }
}
