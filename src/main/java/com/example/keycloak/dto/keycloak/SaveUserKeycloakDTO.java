package com.example.keycloak.dto.keycloak;

import com.example.keycloak.dto.user.UserToUpdateDTO;

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

    public static SaveUserKeycloakDTO fromCreateUserDto(UserToUpdateDTO userDTO){
        return new SaveUserKeycloakDTO(
                null,
                userDTO.email(),
                true,
                true,
                userDTO.lastName(),
                userDTO.lastName(),
                userDTO.email(),
                null,
                null
        );
    }
}
