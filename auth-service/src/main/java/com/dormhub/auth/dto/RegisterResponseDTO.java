package com.dormhub.auth.dto;

import com.dormhub.auth.domain.RoleName;

import java.util.UUID;

public record RegisterResponseDTO(
        UUID userId,
        String email,
        RoleName role
) {
}