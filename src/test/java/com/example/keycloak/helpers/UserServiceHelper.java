package com.example.keycloak.helpers;

import com.example.keycloak.dto.keycloak.IdNameDTO;
import com.example.keycloak.dto.keycloak.UserKeycloakDTO;
import com.example.keycloak.dto.user.CreatePasswordDTO;
import com.example.keycloak.dto.user.UserDTO;
import com.example.keycloak.dto.user.UserToUpdateDTO;
import com.example.keycloak.model.User;

import java.util.Collections;
import java.util.Map;

public class UserServiceHelper {

    public static User getUser() {
        User user = new User();
        user.setId(1l);
        user.setKeycloakId("example");
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail("teste@teste.com");
        user.setVerificationCode("12345");

        return user;
    }

    public static User userToCreate() {
        User user = new User();
        user.setId(2l);
        user.setKeycloakId("example2");
        user.setFirstName("new");
        user.setLastName("user");
        user.setEmail("registered_user@teste.com");

        return user;
    }

    public static UserDTO createdUser() {
        User user = userToCreate();
        return UserDTO.fromUser(user);
    }

    public static UserToUpdateDTO userUpdate() {
        User user = userToCreate();

        return new UserToUpdateDTO(user.getId(), user.getFirstName(), user.getLastName(), new IdNameDTO("id", "name"), user.getEmail());
    }

    public static UserKeycloakDTO userKeycloak() {
        return new UserKeycloakDTO(
                "keycloakId",
                "registered_user@teste.com",
                "new",
                "user",
                "registered_user@teste.com",
                true,
                1000,
                true,
                false,
                Collections.emptyList(),
                Collections.emptyList(),
                1,
                Map.of()
        );
    }

    public static CreatePasswordDTO createPassword(){
        return new CreatePasswordDTO(
                1l,
                "teste@teste.com",
                "123-456-789",
                "test"
        );
    }
}
