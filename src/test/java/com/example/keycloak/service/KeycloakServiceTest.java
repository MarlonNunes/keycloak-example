package com.example.keycloak.service;

import com.example.keycloak.dto.keycloak.IdNameDTO;
import com.example.keycloak.dto.keycloak.KeycloakTokenDTO;
import com.example.keycloak.dto.keycloak.SaveUserKeycloakDTO;
import com.example.keycloak.dto.keycloak.UserKeycloakDTO;
import com.example.keycloak.helpers.KeycloakServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KeycloakServiceTest {

    @Mock
    private RestTemplate restTemplate;
    private KeycloackService service;

    @BeforeEach
    public void init(){
        service = new KeycloackService(restTemplate, "http://localhost:8080/", "keycloakRealm", "keycloakRealmAdmin", "user", "password", "adminClientId");
        lenient().when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(new KeycloakTokenDTO("token", 3600)));
    }

    @Test
    public void createUser_shouldCreateUserAndReturn_whenUserDoesNotExist() {
        // given
        SaveUserKeycloakDTO userToSave = KeycloakServiceHelper.getSaveUserKeycloakDTO();
        List<IdNameDTO> role = Collections.singletonList(new IdNameDTO("id", "name"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))) // save User
                .thenReturn(ResponseEntity.ok(""));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))) // get Email
                .thenReturn(ResponseEntity.ok(Collections.singletonList(KeycloakServiceHelper.getUserKeycloakDTO())));

        // when
        UserKeycloakDTO userKeycloak = service.createUser(userToSave, role).getBody();

        // then
        assertEquals(userKeycloak.username(), userToSave.username());

    }

    @Test
    public void getUserByEmail_shouldReturnUser_whenUserExists() {
        // given
        String email = "registered_user@teste.com";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))) // get Email
                .thenReturn(ResponseEntity.ok(Collections.singletonList(KeycloakServiceHelper.getUserKeycloakDTO())));

        // when
        UserKeycloakDTO userKeycloak = service.getUserByEmail(email).getBody();

        // then
        assertEquals(userKeycloak.email(), email);
        assertNotNull(userKeycloak.id());
    }

    @Test
    public void getUserByEmail_shouldThrowException_whenUserDoesNotExist() {
        // given
        String email = "any_email@teste.com";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))) // get Email
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.getUserByEmail(email);
        });

        // then
        assertEquals(exception.getReason(), "keycloak.exception.user.email-not-found");
        assertEquals(exception.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void assignRoles_shouldAssignRoles_whenUserExists() {
        // given
        String userKeycloakId = "id";
        List<IdNameDTO> roles = Collections.singletonList(new IdNameDTO("id", "name"));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))) // assign roles
                .thenReturn(ResponseEntity.ok(""));

        // when
        ResponseEntity<String> result = service.assignRoles(userKeycloakId, roles);

        // then
        assertNotNull(result);
        assertEquals(result.getStatusCode(), HttpStatus.OK);
    }
}
