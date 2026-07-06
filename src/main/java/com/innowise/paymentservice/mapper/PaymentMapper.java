package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.dto.PaymentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(Payment entity);
}
