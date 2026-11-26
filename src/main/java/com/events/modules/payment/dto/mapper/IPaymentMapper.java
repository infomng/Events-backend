package com.events.modules.payment.dto.mapper;

import com.events.modules.payment.dto.GetPaymentDto;
import com.events.modules.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IPaymentMapper {

    GetPaymentDto toDto(Payment payment);

    List<GetPaymentDto> toDtoList(List<Payment> payments);

}
