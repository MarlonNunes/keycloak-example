package com.example.keycloak.dto.keycloak;

public record CredentialKeycloakDTO(
        String id,
        String type,
        String userLabel,
        String value
) {

    public static CredentialKeycloakDTO update(String id, String password){
        return new CredentialKeycloakDTO(
                id,
                "password",
                "password",
                password
        );
    }

}
