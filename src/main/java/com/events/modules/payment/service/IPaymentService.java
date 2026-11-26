package com.events.modules.payment.service;

import com.events.modules.payment.dto.*;
import com.events.modules.payment.enumeration.PaymentStatusEnum;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IPaymentService {

    /**
     * Initiate a new payment for a booking
     * @param dto payment initiation data
     * @return payment response with redirect URL if needed
     */
    PaymentResponseDto initiatePayment(InitiatePaymentDto dto);

    /**
     * Synchronise a payment with the gateway (status is always read from Stripe)
     * @param dto confirmation data
     * @return the payment status after synchronisation
     */
    PaymentStatusEnum confirmPayment(ConfirmPaymentDto dto);

    /**
     * Handle a signed Stripe webhook (source of truth for payment status)
     * @param payload raw request body
     * @param signatureHeader value of the Stripe-Signature header
     */
    void handleStripeWebhook(String payload, String signatureHeader);

    /**
     * Get payment by ID
     * @param paymentId payment ID
     * @return payment details
     */
    GetPaymentDto getPaymentById(UUID paymentId);

    /**
     * Get payment by reference
     * @param paymentReference payment reference
     * @return payment details
     */
    GetPaymentDto getPaymentByReference(String paymentReference);

    /**
     * Get all payments for current user
     * @param page page number
     * @param size page size
     * @return paginated payments
     */
    Page<GetPaymentDto> getMyPayments(int page, int size);

    /**
     * Get payments by status for current user
     * @param status payment status
     * @return list of payments
     */
    List<GetPaymentDto> getMyPaymentsByStatus(PaymentStatusEnum status);

    /**
     * Cancel a pending payment
     * @param paymentId payment ID
     */
    void cancelPayment(UUID paymentId);

    /**
     * Refund a completed payment
     * @param dto refund data
     */
    void refundPayment(RefundPaymentDto dto);

    /**
     * Check payment status
     * @param paymentReference payment reference
     * @return payment status
     */
    PaymentStatusEnum checkPaymentStatus(String paymentReference);

    /**
     * Rembourse le paiement d'une réservation dont l'événement a été annulé, puis annule la réservation
     * et ses billets. Une réservation gratuite est simplement annulée.
     * @return true si l'acheteur a été remboursé (ou n'avait rien payé), false si le remboursement
     * a échoué et doit être traité manuellement
     */
    boolean refundBookingForCancelledEvent(UUID bookingId);
}
