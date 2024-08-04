package com.example.keycloak.dto.user;

public record CreatePasswordDTO(
        Long userId,
        String email,
        String verificationCode,
        String password
) {
}
