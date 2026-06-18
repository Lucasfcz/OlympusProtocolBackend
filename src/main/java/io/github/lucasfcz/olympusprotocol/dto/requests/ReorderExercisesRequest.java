package io.github.lucasfcz.olympusprotocol.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderExercisesRequest(
        @NotEmpty List<ExerciseOrderItem> orders
) {
    public record ExerciseOrderItem(
            @NotNull UUID exerciseId,
            @NotNull Integer order
    ) {}
}

