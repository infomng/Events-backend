package com.events.common.dto.pagination;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResponse<T>(int page,
                              int size,
                              long totalElements,
                              int totalPages,
                              boolean last,
                              List<T> content) {
}
