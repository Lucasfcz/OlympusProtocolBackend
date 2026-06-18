package io.github.lucasfcz.olympusprotocol.dto.requests;

import java.util.UUID;

public record UpdateSessionExerciseRequest(
        UUID newExerciseId,
        Integer exerciseOrder
) {}
