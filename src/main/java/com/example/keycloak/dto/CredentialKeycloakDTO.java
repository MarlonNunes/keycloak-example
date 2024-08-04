package com.example.keycloak.dto;

public record CredentialKeycloakDTO(
        String id,
        String type,
        String userLabel,
        String value
) {

}
