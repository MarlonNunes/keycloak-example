package com.example.keycloak.dto;

import java.util.List;

public record SaveUserKeycloakDTO(
        String id,
        String username,
        Boolean enabled,
        Boolean emailVerified,
        String firstName,
        String lastName,
        String email,
        List<CredentialKeycloakDTO> credentials,
        List<String> requiredActions
) {
}
