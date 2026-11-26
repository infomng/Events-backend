package com.events.modules.user.controller;

import com.events.common.result.Result;
import com.events.modules.user.dto.ChangeUserRoleDto;
import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final IUserService userService;

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change the role of a user, e.g. promote to ORGANIZER (admin only)")
    public ResponseEntity<Result<GetUserDto>> changeRole(@PathVariable UUID userId,
                                                         @Valid @RequestBody ChangeUserRoleDto dto) {
        return ResponseEntity.ok(Result.success(userService.changeRole(userId, dto.role())));
    }
}
