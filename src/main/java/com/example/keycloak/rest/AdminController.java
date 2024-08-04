package com.example.keycloak.rest;

import com.example.keycloak.dto.keycloak.IdNameDTO;
import com.example.keycloak.dto.user.UserDTO;
import com.example.keycloak.dto.user.UserToUpdateDTO;
import com.example.keycloak.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.IDN;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserToUpdateDTO userToUpdateDTO){
        return this.userService.createUser(userToUpdateDTO);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<IdNameDTO>> getRoles(){
        return this.userService.getAllRoles();
    }
}
