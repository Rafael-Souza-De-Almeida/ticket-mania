package com.github.rafael_souza_de_almeida.ticket_mania.user.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.AuthenticationDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.LoginResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.RegisterDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.service.AuthService;
import com.github.rafael_souza_de_almeida.ticket_mania.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e cadastro de usuários")
public class AuthenticationController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida as credenciais do usuário e retorna um token JWT para acessar a API.")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid AuthenticationDto data) {
        String token = authService.authenticate(data);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuário", description = "Cria um novo usuário no sistema.")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDto data) {
        userService.registerUser(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
