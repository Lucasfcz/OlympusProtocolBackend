package io.github.lucasfcz.olympusprotocol.dto.responses;

import java.util.UUID;

public record WorkoutSessionSetResponse(
        UUID id,
        Integer setOrder,
        Integer reps,
        Double weight,
        Integer restTIme
) {}