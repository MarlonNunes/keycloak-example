package com.example.keycloak.dto.user;

import com.example.keycloak.dto.keycloak.IdNameDTO;

public record UserToUpdateDTO(
        Long id,
        String firstName,
        String lastName,
        IdNameDTO role,
        String email
) {
}
