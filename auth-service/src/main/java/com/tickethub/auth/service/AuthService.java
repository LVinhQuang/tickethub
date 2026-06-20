package com.tickethub.auth.service;

import com.tickethub.auth.domain.Role;
import com.tickethub.auth.domain.RoleName;
import com.tickethub.auth.domain.User;
import com.tickethub.auth.dto.LoginRequestDTO;
import com.tickethub.auth.dto.LoginResponseDTO;
import com.tickethub.auth.dto.RegisterRequestDTO;
import com.tickethub.auth.dto.RegisterResponseDTO;
import com.tickethub.auth.repository.RoleRepository;
import com.tickethub.auth.repository.UserRepository;
import com.tickethub.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new IllegalStateException("Default STUDENT role not found"));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(studentRole);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return new RegisterResponseDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().getName()
        );
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);

        return new LoginResponseDTO(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getEmail(),
                user.getRole().getName()
        );
    }
}