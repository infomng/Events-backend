package com.events.modules.payment.service;
//
//import com.events.modules.auth.service.auth.IAuthService;
//import com.events.modules.booking.dto.GetBookingDto;
//import com.events.modules.booking.entity.Booking;
//import com.events.modules.booking.service.IBookingService;
//import com.events.modules.event.enumeration.BookingStatusEnum;
//import com.events.modules.payment.dto.CreatePaymentDto;
//import com.events.modules.payment.entity.Payment;
//import com.events.modules.payment.repository.IPaymentRepository;
//import com.events.modules.ticket.service.ITicketService;
//import com.events.modules.user.entity.User;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PaymentService implements IPaymentService {
//
//    private final IPaymentRepository paymentRepository;
//    private final IBookingService bookingService;
//    private final IAuthService authService;
//    private final ITicketService ticketService;
//
//    @Override
//    public UUID createPayment(CreatePaymentDto dto) {
//        User currentUser = authService.getCurrentUser();
//        GetBookingDto booking = bookingService.findById(dto.bookingId());
//
//        // TODO: Add more logic, e.g., check if booking is already paid
//
//        Payment payment = Payment.builder()
//                .user(currentUser)
//                .booking(booking)
//                .amount(dto.amount())
//                .currency(dto.currency())
//                .status("SUCCESS") // Or an enum
//                .build();
//
//        payment = paymentRepository.save(payment);
//
//        booking.setStatus(BookingStatusEnum.CONFIRMED);
////        bookingService.save(booking);
//
//        ticketService.generateTicketsForBooking(booking);
//
//        return payment.getId();
//    }
//}
