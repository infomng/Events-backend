package com.events.modules.payment.controller;

import com.events.common.result.Result;
import com.events.modules.payment.dto.CreatePaymentDto;
import com.events.modules.payment.service.IPaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payments management APIs")
@RequiredArgsConstructor
public class PaymentController {

//    private final IPaymentService paymentService;
//
//    @PostMapping
//    public ResponseEntity<Result<UUID>> createPayment(@Valid @RequestBody CreatePaymentDto dto) {
//        UUID paymentId = paymentService.createPayment(dto);
//        return ResponseEntity.ok(Result.success(paymentId));
//    }
}
