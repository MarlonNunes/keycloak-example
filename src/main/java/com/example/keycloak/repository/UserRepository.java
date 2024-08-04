package com.example.keycloak.repository;

import com.example.keycloak.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKeycloakId(String id);

    List<User> findByEmail(String email);
}
