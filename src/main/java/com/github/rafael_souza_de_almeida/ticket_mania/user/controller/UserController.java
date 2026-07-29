package com.github.rafael_souza_de_almeida.ticket_mania.user.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.UpdateRoleDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários e papéis")
public class UserController {

    private final UserService userService;

    @PatchMapping("/{id}/role")
    @Operation(summary = "Atualizar papel do usuário", description = "Altera o papel de um usuário existente.")
    public ResponseEntity<Void> updateRole(@PathVariable UUID id, @RequestBody UpdateRoleDto dto) {

        userService.updateUserRole(id, dto.role());
        return ResponseEntity.noContent().build();

    }


}
