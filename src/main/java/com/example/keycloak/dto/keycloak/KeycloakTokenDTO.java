package com.example.keycloak.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakTokenDTO(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        Integer expiresIn
) {
}
