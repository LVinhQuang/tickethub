package com.tickethub.auth.dto;

import com.tickethub.auth.domain.RoleName;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        RoleName role
) {
}