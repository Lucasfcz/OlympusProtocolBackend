package io.github.lucasfcz.olympusprotocol.dto.requests;

public record AddSetRequest(
        Integer reps,
        Double weight,
        Integer restTime,
        Integer setOrder
) {
}
