package com.example.keycloak.service;

import com.example.keycloak.dto.keycloak.IdNameDTO;
import com.example.keycloak.dto.keycloak.SaveUserKeycloakDTO;
import com.example.keycloak.dto.keycloak.UserKeycloakDTO;
import com.example.keycloak.dto.user.CreatePasswordDTO;
import com.example.keycloak.dto.user.UserDTO;
import com.example.keycloak.dto.user.UserToUpdateDTO;
import com.example.keycloak.model.User;
import com.example.keycloak.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final KeycloackService keycloackService;

    @PostConstruct
    public void addDefaultUser(){
        User user = new User();
        user.setId(1l);
        user.setFirstName("Marlon");
        user.setLastName("Nunes");
        user.setKeycloakId("730df55b-8ddf-4af4-82fe-06903b879537");

        repository.save(user);
    }

    public User getUserByKeycloakId(String id){
        return repository.findByKeycloakId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user.not-found"));
    }

    private void checkIfUserIsAlreadyRegistered(UserToUpdateDTO userDTO){
        List<User> users = this.repository.findByEmail(userDTO.email());

        if(!users.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "service.user-service.default.already-exists");
        }
    }

    public ResponseEntity<UserDTO> createUser(UserToUpdateDTO userDTO){
        checkIfUserIsAlreadyRegistered(userDTO);

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        user.setCreatedAt(LocalDateTime.now());

        UserKeycloakDTO userKeycloak = this.keycloackService.createUser(SaveUserKeycloakDTO.fromCreateUserDto(userDTO), List.of(userDTO.role())).getBody();
        user.setKeycloakId(userKeycloak.id());
        user.setVerificationCode(UUID.randomUUID().toString());
        user.setVerificationCodeValidUntil(LocalDateTime.now().plusDays(2));

        user = repository.save(user);

        return new ResponseEntity<>(UserDTO.fromUser(user), HttpStatus.CREATED);
    }

    public ResponseEntity<Void> createPassword(CreatePasswordDTO createPassword) {

        User user = this.repository.getById(createPassword.userId());

        if(!createPassword.verificationCode().equals(user.getVerificationCode())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user.verification-code.invalid");
        }

        if(user.getVerificationCodeValidUntil().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user.verification-code.expired");
        }

        ResponseEntity<Void> result = this.keycloackService.resetPassword(user.getKeycloakId(), null, createPassword.password());

        user.setVerificationCodeValidUntil(null);
        user.setVerificationCode(null);

        repository.save(user);

        return result;
    }

    public ResponseEntity<List<IdNameDTO>> getAllRoles(){
        return this.keycloackService.getAllRoles();
    }

    public ResponseEntity<UserDTO> getMe(User user) {
        return new ResponseEntity<>(UserDTO.fromUser(user), HttpStatus.OK);
    }
}
