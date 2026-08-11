package com.tickethub.auth.dto;

public record MeResponse(
    String email,
    String role
) {
}
