package com.example.keycloak.dto;

import java.util.List;
import java.util.Map;

public record UserKeycloakDTO(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        boolean emailVerified,
        long createdTimestamp,
        boolean enabled,
        boolean totp,
        List<String> disableableCredentialTypes,
        List<String> requiredActions,
        int notBefore,
        Map<String, Boolean> access
) {
}
