package com.example.keycloak.dto.user;

import com.example.keycloak.model.User;

public record UserDTO (
        Long id,
        String name,
        String email,
        String verificationCode,
        String role
){
    public static UserDTO fromUser(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getVerificationCode(),
                user.getRole()
        );
    }
}
