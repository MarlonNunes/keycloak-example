package com.example.keycloak.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String keycloakId;
    private String verificationCode;
    private LocalDateTime verificationCodeValidUntil;
    private LocalDateTime createdAt;
    @Transient private String role;
}
