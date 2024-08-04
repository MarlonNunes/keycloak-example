package com.example.keycloak.service;

import com.example.keycloak.dto.keycloak.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class KeycloackService {

    @Value("${keycloak.url}")
    private final String keycloakUrl;
    @Value("${keycloak.realm}")
    private final String keycloakRealm;
    @Value("${keycloak.realm-admin}")
    private final String keycloakRealmAdmin;
    @Value("${keycloak.admin-user}")
    private final String user;
    @Value("${keycloak.admin-password}")
    private final String password;
    @Value("${keycloak.admin-client-id}")
    private final String adminClientId;
    private String token;
    private LocalDateTime tokenExpiration;
    private final RestTemplate restTemplate;
    private final String USERS_ENDPOINT;

    public KeycloackService(RestTemplate restTemplate,
                            @Value("${keycloak.url}") String keycloakUrl,
                            @Value("${keycloak.realm}") String keycloakRealm,
                            @Value("${keycloak.realm-admin}") String keycloakRealmAdmin,
                            @Value("${keycloak.admin-user}") String user,
                            @Value("${keycloak.admin-password}") String password,
                            @Value("${keycloak.admin-client-id}") String adminClientId) {
        this.restTemplate = restTemplate;
        this.keycloakUrl = keycloakUrl;
        this.keycloakRealm = keycloakRealm;
        this.keycloakRealmAdmin = keycloakRealmAdmin;
        this.user = user;
        this.password = password;
        this.adminClientId = adminClientId;
        USERS_ENDPOINT = "admin/realms/" + keycloakRealm + "/users";

    }

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + this.getToken());

        return headers;
    }

    private String getToken(){
        if(Objects.isNull(token) || Objects.isNull(this.tokenExpiration) || LocalDateTime.now().isAfter(this.tokenExpiration)){
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", adminClientId);
            formData.add("username", user);
            formData.add("password", password);
            formData.add("grant_type", "password");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

            StringBuilder builder = new StringBuilder();
            builder.append(this.keycloakUrl)
                    .append("realms/")
                    .append(this.keycloakRealmAdmin)
                    .append("/protocol/openid-connect/token");

            KeycloakTokenDTO result = restTemplate.postForEntity(builder.toString(), entity, KeycloakTokenDTO.class).getBody();

            this.token = result.accessToken();
            this.tokenExpiration = Optional
                    .ofNullable(result.expiresIn())
                    .map( expiration -> LocalDateTime.now().plusSeconds(expiration))
                    .orElse(null);
        }

        return this.token;
    }

    public ResponseEntity<UserKeycloakDTO> createUser(SaveUserKeycloakDTO user, List<IdNameDTO> roles){
        HttpHeaders headers = this.getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SaveUserKeycloakDTO> entity = new HttpEntity<>(user, headers);


        ResponseEntity<String> result = null;

        try{
            result = restTemplate.exchange(this.keycloakUrl + this.USERS_ENDPOINT, HttpMethod.POST, entity, String.class);
        }catch (HttpClientErrorException e){
            log.error("An error occurred when create user: {}", user, e);
            throw new ResponseStatusException(e.getStatusCode(), "keycloak.error.save-user");
        }

        ResponseEntity<UserKeycloakDTO> userKeycloak = this.getUserByEmail(user.email());

        this.assignRoles(userKeycloak.getBody().id(), roles);

        return userKeycloak;
    }

    public ResponseEntity<UserKeycloakDTO> getUserByEmail(String email){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(this.keycloakUrl + this.USERS_ENDPOINT)
                .queryParam("email", email);

        HttpHeaders headers = this.getHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);


        ResponseEntity<List<UserKeycloakDTO>> result = null;
        try {
            result = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e){
            log.error("An error occurred when fetching user with email: {}", email, e);
            throw new ResponseStatusException(e.getStatusCode(), "keycloak.error.users.by-email");
        }
        List<UserKeycloakDTO> users = result.getBody();
        if(users.isEmpty()){
            log.error("no results for this email {} on keycloak", email);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "keycloak.exception.user.email-not-found");
        }else if(users.size() > 1){
            log.warn("More than one result for this email {} on keycloak. Returning only first", email);
        }

        return ResponseEntity.ok(users.get(0));
    }

    public ResponseEntity<String> assignRoles(String userKeycloakId, List<IdNameDTO> roles){
        final String path = "admin/realms/" + keycloakRealm + "/users/" + userKeycloakId + "/role-mappings/realm";
        HttpHeaders headers = this.getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<IdNameDTO>> entity = new HttpEntity<>(roles, headers);

        ResponseEntity<String> result = null;

        try{
            result = restTemplate.exchange(this.keycloakUrl + path, HttpMethod.POST, entity, String.class);
        }catch (HttpClientErrorException e){
            log.error("An error occurred when assing role: {} for user: {}", roles, userKeycloakId, e);
            throw new ResponseStatusException(e.getStatusCode(), "keycloak.error.roles.assign");
        }

        return result;
    }

    public ResponseEntity<Void> resetPassword(String userKeycloakId, String credentialId, String password){
        final String path = "admin/realms/" + keycloakRealm + "/users/" + userKeycloakId + "/reset-password";

        HttpHeaders headers = this.getHeaders();

        HttpEntity<CredentialKeycloakDTO> entity = new HttpEntity<>(CredentialKeycloakDTO.update(credentialId, password), headers);

        ResponseEntity<Void> result = null;

        try{
            result = restTemplate.exchange(this.keycloakUrl + path, HttpMethod.PUT, entity, Void.class);
        }catch (HttpClientErrorException e){
            log.error("An error occurred while resetting the user password: {}", userKeycloakId, e);
            throw new ResponseStatusException(e.getStatusCode(), "keycloak.error.users.reset-password");
        }

        return result;
    }

    public ResponseEntity<List<IdNameDTO>> getAllRoles(){
        final String path = "admin/realms/" + this.keycloakRealm + "/roles";

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(this.keycloakUrl + path)
                .queryParam("first", 0)
                .queryParam("max", 100);


        HttpHeaders headers = this.getHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);


        ResponseEntity<IdNameDTO[]> result = null;
        try {
            result = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity,  IdNameDTO[].class);
        } catch (HttpClientErrorException e){
            log.error("An error occurred when fetching roles", e);
            throw new ResponseStatusException(e.getStatusCode(), "keycloak.error.roles.get-all");
        }

        return ResponseEntity.ok(List.of(result.getBody()));
    }
}
