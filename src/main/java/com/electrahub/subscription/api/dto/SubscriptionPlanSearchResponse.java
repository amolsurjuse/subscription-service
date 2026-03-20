package com.electrahub.subscription.api.dto;

import java.util.List;

public record SubscriptionPlanSearchResponse(
        List<SubscriptionPlanResponse> items,
        long total,
        int limit,
        int offset,
        int currentPage,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
