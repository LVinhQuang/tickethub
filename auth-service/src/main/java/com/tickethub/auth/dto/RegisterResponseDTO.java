package com.tickethub.auth.dto;

import com.tickethub.auth.domain.RoleName;

import java.util.UUID;

public record RegisterResponseDTO(
        UUID userId,
        String email,
        RoleName role
) {
}