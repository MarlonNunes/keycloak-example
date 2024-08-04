package com.example.keycloak.rest;

import com.example.keycloak.dto.user.CreatePasswordDTO;
import com.example.keycloak.dto.user.UserDTO;
import com.example.keycloak.model.User;
import com.example.keycloak.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    @PostMapping("/create-password")
    public ResponseEntity<Void> createPassword(@RequestBody CreatePasswordDTO passwordDTO){
        return this.userService.createPassword(passwordDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe(@AuthenticationPrincipal User user){
        return this.userService.getMe(user);
    }
}
