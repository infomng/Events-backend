package com.events.modules.booking.controller;

import com.events.common.result.Result;
import com.events.modules.booking.dto.CheckoutRequestDto;
import com.events.modules.booking.dto.GetBookingDto;
import com.events.modules.booking.dto.SalesReportDto;
import com.events.modules.booking.service.IBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking")
public class BookingController {

    private final IBookingService bookingService;

    @PostMapping("/checkout")
    @Operation(summary = "Turn the cart items of an event into a booking and hold the tickets")
    public ResponseEntity<Result<GetBookingDto>> checkout(@Valid @RequestBody CheckoutRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(bookingService.checkout(dto)));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "List my bookings")
    public ResponseEntity<Result<Page<GetBookingDto>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(Result.success(bookingService.getMyBookings(page, size)));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get one of my bookings")
    public ResponseEntity<Result<GetBookingDto>> getMyBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(Result.success(bookingService.getMyBooking(bookingId)));
    }

    @GetMapping("/sales/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(summary = "Sales dashboard of an event (organizer or admin)")
    public ResponseEntity<Result<SalesReportDto>> getSalesReport(@PathVariable UUID eventId) {
        return ResponseEntity.ok(Result.success(bookingService.getSalesReport(eventId)));
    }

    @PatchMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel one of my unpaid bookings and release its tickets")
    public ResponseEntity<Result<Void>> cancelMyBooking(@PathVariable UUID bookingId) {
        bookingService.cancelMyBooking(bookingId);
        return ResponseEntity.ok(Result.success());
    }
}
