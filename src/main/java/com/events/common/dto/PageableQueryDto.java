package com.events.common.dto;

import jakarta.validation.constraints.Min;

public record PageableQueryDto(
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sort,
        String direction
) {}

