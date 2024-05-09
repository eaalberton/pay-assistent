package com.br.api.payassistent.controller;

import com.br.api.payassistent.config.UserAuthenticationProvider;
import com.br.api.payassistent.model.dto.CredentialsDto;
import com.br.api.payassistent.model.dto.SignUpDto;
import com.br.api.payassistent.model.dto.UserDto;
import com.br.api.payassistent.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;
    private final UserAuthenticationProvider userAuthenticationProvider;

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        UserDto userDto = userService.login(credentialsDto);
        userDto.setToken(userAuthenticationProvider.createToken(userDto));
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid SignUpDto user) {
        UserDto createdUser = userService.register(user);
        createdUser.setToken(userAuthenticationProvider.createToken(createdUser));
        return ResponseEntity.created(URI.create("/users/" + createdUser.getId())).body(createdUser);
    }

    @GetMapping("/validate")
    public ResponseEntity<List<String>> validate() {
            return ResponseEntity.ok(Arrays.asList("Token valid"));
    }

    @GetMapping("/find-user-by-id")
    public ResponseEntity<UserDto> findById(@RequestParam String id) {
        UserDto userDto = userService.findById(id);
        return ResponseEntity.ok(userDto);
    }

}
