package com.dormhub.auth.service;

import com.dormhub.auth.domain.Role;
import com.dormhub.auth.domain.RoleName;
import com.dormhub.auth.domain.User;
import com.dormhub.auth.dto.LoginRequestDTO;
import com.dormhub.auth.dto.LoginResponseDTO;
import com.dormhub.auth.dto.RegisterRequestDTO;
import com.dormhub.auth.dto.RegisterResponseDTO;
import com.dormhub.auth.repository.RoleRepository;
import com.dormhub.auth.repository.UserRepository;
import com.dormhub.auth.security.JwtService;
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