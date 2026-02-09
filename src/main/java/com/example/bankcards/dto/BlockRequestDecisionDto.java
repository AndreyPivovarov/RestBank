package com.example.bankcards.dto;

import jakarta.validation.constraints.Size;

public record BlockRequestDecisionDto(
        @Size(max = 500) String comment
) {}
