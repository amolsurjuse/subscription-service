package com.electrahub.subscription.api.dto;

import com.electrahub.subscription.domain.AllocationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAllocationStatusRequest(
        @NotNull AllocationStatus status,
        @NotBlank String actor,
        @Size(max = 512) String reason
) {
}
