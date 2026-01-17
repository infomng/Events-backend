package com.events.modules.payment.service;

import com.events.modules.payment.dto.CreatePaymentDto;

import java.util.UUID;

public interface IPaymentService {
    UUID createPayment(CreatePaymentDto dto);
}
