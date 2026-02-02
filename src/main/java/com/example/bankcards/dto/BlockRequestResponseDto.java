package com.example.bankcards.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BlockRequestResponseDto(
        UUID id,
        UUID cardId,
        String status,
        String comment,
        LocalDateTime createdAt
) {
}