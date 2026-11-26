package com.events.modules.booking.service;

import com.events.common.exception.BadRequestException;
import com.events.modules.booking.dto.BookingPaymentDto;
import com.events.modules.booking.entity.Booking;
import com.events.modules.booking.repository.IBookingRepository;
import com.events.modules.event.enumeration.BookingStatusEnum;
import com.events.modules.event.enumeration.PaymentStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService implements IBookingService {

    private final IBookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public BookingPaymentDto getPayableBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUserId().equals(userId))
                // Même message si absente ou appartenant à un autre utilisateur : pas de fuite d'information
                .orElseThrow(() -> new BadRequestException("Booking not found: " + bookingId));

        if (booking.getStatus() != BookingStatusEnum.PENDING) {
            throw new BadRequestException("Booking is not awaiting payment: " + bookingId);
        }

        if (booking.getTotalAmount() == null || booking.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Booking has no payable amount: " + bookingId);
        }

        return new BookingPaymentDto(
                booking.getId(),
                booking.getUserId(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getBookingReference()
        );
    }

    @Override
    public void confirmBookingPayment(UUID bookingId, String paymentId) {
        Booking booking = getBooking(bookingId);
        booking.setStatus(BookingStatusEnum.CONFIRMED);
        booking.setPaymentStatus(PaymentStatusEnum.SUCCESS);
        booking.setPaymentId(paymentId);
        bookingRepository.save(booking);
    }

    @Override
    public void cancelBookingAfterRefund(UUID bookingId) {
        Booking booking = getBooking(bookingId);
        booking.setStatus(BookingStatusEnum.CANCELLED);
        bookingRepository.save(booking);
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BadRequestException("Booking not found: " + bookingId));
    }
}
