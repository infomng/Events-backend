package com.events.modules.ticket.controller;

import com.events.common.result.Result;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.ticket.dto.TicketDto;
import com.events.modules.ticket.service.ITicketService;
import com.events.modules.user.entity.User;
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

    @GetMapping("/my-tickets")
    public ResponseEntity<Result<List<TicketDto>>> getMyTickets() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(Result.success(ticketService.getTicketsForUser(currentUser.getId())));
    }
}
