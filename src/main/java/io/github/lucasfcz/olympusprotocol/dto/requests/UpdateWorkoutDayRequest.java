package io.github.lucasfcz.olympusprotocol.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkoutDayRequest(
        @NotNull String name,
        @NotNull @Min(1) Integer dayOrder
) {}

