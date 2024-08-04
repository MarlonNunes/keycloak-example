package com.example.keycloak.service;

import com.example.keycloak.dto.user.CreatePasswordDTO;
import com.example.keycloak.dto.user.UserDTO;
import com.example.keycloak.dto.user.UserToUpdateDTO;
import com.example.keycloak.helpers.UserServiceHelper;
import com.example.keycloak.model.User;
import com.example.keycloak.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository repository;
    @Mock
    private KeycloackService keycloackService;
    private UserService service;

    @BeforeEach
    public void init(){
        service = new UserService(repository, keycloackService);
        lenient().when(repository.getById(1l)).thenReturn(UserServiceHelper.getUser());
        lenient().when(repository.findByKeycloakId("example")).thenReturn(Optional.of(UserServiceHelper.getUser()));
        lenient().when(repository.findByEmail("teste@teste.com")).thenReturn(List.of(UserServiceHelper.getUser()));
        lenient().when(repository.save(any())).thenReturn(UserServiceHelper.userToCreate());

    }

    @Test
    public void getUserByKeycloakId_shouldReturnUser_WhenUserExists(){
        //when
        User foundUser = service.getUserByKeycloakId("example");

        //then
        assertEquals(foundUser.getKeycloakId(), "example");
        assertEquals(foundUser.getId(), 1l);
    }

    @Test
    public void getUserByKeycloakId_shouldThrowException_whenUserDoesNotExist(){
        // given
        when(repository.findByKeycloakId("test")).thenReturn(Optional.ofNullable(null));

        //when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.getUserByKeycloakId("test");
        });

        //then
        assertEquals(exception.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(exception.getReason(), "user.not-found");
    }

    @Test
    public void createUser_shouldThrowException_whenUserAlreadyRegistered(){
        //given
        UserToUpdateDTO userToUpdate = new UserToUpdateDTO(null, "Test", "Example", null, "teste@teste.com");

        //when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.createUser(userToUpdate);
        });

        //then
        assertEquals(exception.getReason(), "service.user-service.default.already-exists");
        assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST);
    }


    @Test
    public void createUser_shouldCreateUser_whenUserNotRegistered(){
        //given
        UserToUpdateDTO userToUpdate = UserServiceHelper.userUpdate();
        when(keycloackService.createUser(any(), any())).thenReturn(ResponseEntity.ok(UserServiceHelper.userKeycloak()));

        //when
        UserDTO userDTO = this.service.createUser(userToUpdate).getBody();

        //then
        assertEquals(userDTO.id(), userToUpdate.id());
        assertEquals(userDTO.name(), userToUpdate.firstName() + " " + userToUpdate.lastName());
    }

    @Test
    public void createUser_shouldThrowException_whenVerificationCodeInvalid(){
        //given
        CreatePasswordDTO createPassword = UserServiceHelper.createPassword();

        //when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            this.service.createPassword(createPassword);
        });

        assertEquals(exception.getStatusCode(),  HttpStatus.BAD_REQUEST);
        assertEquals(exception.getReason(), "user.verification-code.invalid");
    }

    @Test
    public void createUser_shouldThrowException_whenVerificationCodeExpired(){
        //given
        CreatePasswordDTO createPassword = UserServiceHelper.createPassword();
        User user = UserServiceHelper.getUser();
        user.setVerificationCodeValidUntil(LocalDateTime.now().minusDays(1));
        user.setVerificationCode(createPassword.verificationCode());
        when(repository.getById(1l)).thenReturn(user);

        //when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            this.service.createPassword(createPassword);
        });

        assertEquals(exception.getStatusCode(),  HttpStatus.BAD_REQUEST);
        assertEquals(exception.getReason(), "user.verification-code.expired");
    }

    @Test
    public void createUser_shouldResetPassword_whenVerificationCodeValid(){
        //given
        CreatePasswordDTO createPassword = UserServiceHelper.createPassword();
        User user = UserServiceHelper.getUser();
        user.setVerificationCodeValidUntil(LocalDateTime.now().plusDays(1));
        user.setVerificationCode(createPassword.verificationCode());
        when(repository.getById(1l)).thenReturn(user);
        when(keycloackService.resetPassword(any(), any(), any())).thenReturn(ResponseEntity.ok(null));


        //when
        ResponseEntity<Void> result = service.createPassword(createPassword);

        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

}
