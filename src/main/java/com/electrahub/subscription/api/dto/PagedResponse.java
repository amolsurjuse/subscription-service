package com.electrahub.subscription.api.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> items,
        long total,
        int limit,
        int offset,
        int currentPage,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
