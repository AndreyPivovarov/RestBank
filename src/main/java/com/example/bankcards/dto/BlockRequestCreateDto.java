package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BlockRequestCreateDto(
        @NotNull UUID cardId,
        @Size(max = 500) String comment
) {
}
