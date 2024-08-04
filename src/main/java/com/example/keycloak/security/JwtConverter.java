package com.example.keycloak.security;

import com.example.keycloak.service.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.example.keycloak.model.User;

import java.util.Collection;
import java.util.Map;


public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserService userService;

    public JwtConverter(UserService service){
        userService =  service;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        String sub = source.getClaim("sub");
        User user = this.userService.getUserByKeycloakId(sub);


        Map<String, Collection<String>> realm = source.getClaim("realm_access");

        Collection<String> roles = realm.get("roles");

        var authorities = roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
        user.setRole(roles.stream().filter(name -> !name.contains("default")).findFirst().orElse(null));

        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }
}
