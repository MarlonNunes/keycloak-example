package com.example.keycloak.helpers;

import com.example.keycloak.dto.keycloak.SaveUserKeycloakDTO;
import com.example.keycloak.dto.keycloak.UserKeycloakDTO;
import com.example.keycloak.dto.user.UserToUpdateDTO;

public class KeycloakServiceHelper {

    public static SaveUserKeycloakDTO getSaveUserKeycloakDTO() {
        UserToUpdateDTO user = UserServiceHelper.userUpdate();
        return SaveUserKeycloakDTO.fromCreateUserDto(user);
    }

    public static UserKeycloakDTO getUserKeycloakDTO() {
        return new UserKeycloakDTO(
                "id",
                "registered_user@teste.com",
                "firstName",
                "lastName",
                "registered_user@teste.com",
                true,
                1,
                true,
                true,
                null,
                null,
                1,
                null
        );
    }
}
