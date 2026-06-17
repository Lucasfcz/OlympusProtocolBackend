package io.github.lucasfcz.olympusprotocol.dto.requests;

import java.util.UUID;

public record AddExerciseToSessionRequest(
        UUID exerciseId,
        Integer exerciseOrder
) {
}
