package com.events.modules.event.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Ajout d'un contrôleur (staff) à un événement, identifié par l'email de son compte.
 */
public record AddEventStaffDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
