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
     * Confirm a payment (webhook or manual confirmation)
     * @param dto confirmation data
     */
    void confirmPayment(ConfirmPaymentDto dto);

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
}
