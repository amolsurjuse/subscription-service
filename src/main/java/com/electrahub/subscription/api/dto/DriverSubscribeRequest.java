package com.electrahub.subscription.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DriverSubscribeRequest(@NotNull UUID planId) {
}
