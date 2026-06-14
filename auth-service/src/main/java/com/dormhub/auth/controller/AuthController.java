package com.dormhub.auth.controller;

import com.dormhub.auth.dto.LoginRequestDTO;
import com.dormhub.auth.dto.LoginResponseDTO;
import com.dormhub.auth.dto.RegisterRequestDTO;
import com.dormhub.auth.dto.RegisterResponseDTO;
import com.dormhub.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}