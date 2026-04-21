package com.events.modules.payment.controller;

import com.events.common.result.Result;
import com.events.modules.payment.dto.*;
import com.events.modules.payment.enumeration.PaymentStatusEnum;
import com.events.modules.payment.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment processing APIs")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate a new payment for a booking")
    public ResponseEntity<Result<PaymentResponseDto>> initiatePayment(
            @Valid @RequestBody InitiatePaymentDto dto) {
        PaymentResponseDto response = paymentService.initiatePayment(dto);
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm a payment (webhook or manual)")
    public ResponseEntity<Result<Void>> confirmPayment(
            @Valid @RequestBody ConfirmPaymentDto dto) {
        paymentService.confirmPayment(dto);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<Result<GetPaymentDto>> getPaymentById(
            @PathVariable UUID paymentId) {
        GetPaymentDto payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(Result.success(payment));
    }

    @GetMapping("/reference/{paymentReference}")
    @Operation(summary = "Get payment by reference")
    public ResponseEntity<Result<GetPaymentDto>> getPaymentByReference(
            @PathVariable String paymentReference) {
        GetPaymentDto payment = paymentService.getPaymentByReference(paymentReference);
        return ResponseEntity.ok(Result.success(payment));
    }

    @GetMapping("/my-payments")
    @Operation(summary = "Get all my payments")
    public ResponseEntity<Result<Page<GetPaymentDto>>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GetPaymentDto> payments = paymentService.getMyPayments(page, size);
        return ResponseEntity.ok(Result.success(payments));
    }

    @GetMapping("/my-payments/status/{status}")
    @Operation(summary = "Get my payments by status")
    public ResponseEntity<Result<List<GetPaymentDto>>> getMyPaymentsByStatus(
            @PathVariable PaymentStatusEnum status) {
        List<GetPaymentDto> payments = paymentService.getMyPaymentsByStatus(status);
        return ResponseEntity.ok(Result.success(payments));
    }

    @DeleteMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel a pending payment")
    public ResponseEntity<Result<Void>> cancelPayment(
            @PathVariable UUID paymentId) {
        paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(Result.success());
    }

    @PostMapping("/refund")
    @Operation(summary = "Refund a completed payment")
    public ResponseEntity<Result<Void>> refundPayment(
            @Valid @RequestBody RefundPaymentDto dto) {
        paymentService.refundPayment(dto);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/status/{paymentReference}")
    @Operation(summary = "Check payment status")
    public ResponseEntity<Result<PaymentStatusEnum>> checkPaymentStatus(
            @PathVariable String paymentReference) {
        PaymentStatusEnum status = paymentService.checkPaymentStatus(paymentReference);
        return ResponseEntity.ok(Result.success(status));
    }
}
