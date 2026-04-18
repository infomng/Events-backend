package com.events.modules.event.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageDto(
        @NotBlank
        String url) {
}
