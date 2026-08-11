package com.tickethub.auth.dto;

import com.tickethub.auth.domain.RoleName;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        String email,
        RoleName role
) {
}