package com.events.modules.ticket.controller;

import com.events.common.result.Result;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.ticket.dto.CheckInRequestDto;
import com.events.modules.ticket.dto.CheckInResultDto;
import com.events.modules.ticket.dto.CheckInStatsDto;
import com.events.modules.ticket.dto.TicketDto;
import com.events.modules.ticket.service.ITicketService;
import com.events.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final ITicketService ticketService;
    private final IAuthService authService;

    @GetMapping("/{ticketId}")
    public ResponseEntity<Result<TicketDto>> getTicketById(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(Result.success(ticketService.getTicketById(ticketId)));
    }

    @PostMapping("/check-in")
    @Operation(summary = "Scan a ticket QR code at the entrance (organizer, event staff or admin)")
    public ResponseEntity<Result<CheckInResultDto>> checkIn(@Valid @RequestBody CheckInRequestDto dto) {
        return ResponseEntity.ok(Result.success(ticketService.checkIn(dto)));
    }

    @GetMapping("/check-in/stats/{eventId}")
    @Operation(summary = "Entrance statistics of an event (organizer, event staff or admin)")
    public ResponseEntity<Result<CheckInStatsDto>> getCheckInStats(@PathVariable UUID eventId) {
        return ResponseEntity.ok(Result.success(ticketService.getCheckInStats(eventId)));
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<Result<List<TicketDto>>> getMyTickets() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(Result.success(ticketService.getTicketsForUser(currentUser.getId())));
    }
}
