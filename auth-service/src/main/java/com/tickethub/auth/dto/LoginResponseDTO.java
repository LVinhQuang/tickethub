package com.dormhub.auth.dto;

import com.dormhub.auth.domain.RoleName;

public record LoginResponseDTO(
        String accessToken,
        String tokenType,
        Long expiresIn,
        String email,
        RoleName role
) {
}