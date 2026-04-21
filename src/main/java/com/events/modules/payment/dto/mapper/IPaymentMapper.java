package com.events.modules.payment.dto.mapper;

import com.events.modules.payment.dto.GetPaymentDto;
import com.events.modules.payment.dto.InitiatePaymentDto;
import com.events.modules.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IPaymentMapper {

    GetPaymentDto toDto(Payment payment);

    List<GetPaymentDto> toDtoList(List<Payment> payments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "paymentReference", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "gatewayResponse", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    Payment toEntity(InitiatePaymentDto dto);
}
