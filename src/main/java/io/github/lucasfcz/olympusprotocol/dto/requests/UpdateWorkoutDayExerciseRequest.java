package io.github.lucasfcz.olympusprotocol.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateWorkoutDayExerciseRequest(
        @NotNull UUID exerciseId,
        @NotNull @Min(1) Integer exerciseOrder,
        @NotNull @Min(1) Integer sets,
        @NotNull @Min(1) Integer reps,
        @NotNull @Min(0) Integer restTime
) {}

