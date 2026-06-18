package io.github.lucasfcz.olympusprotocol.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReorderDaysRequest(
        @NotEmpty List<DayOrderItem> orders
) {
    public record DayOrderItem(
            @NotNull UUID dayId,
            @NotNull Integer order
    ) {}
}

