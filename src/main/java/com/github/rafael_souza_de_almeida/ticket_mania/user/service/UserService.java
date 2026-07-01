package com.github.rafael_souza_de_almeida.ticket_mania.user.service;

import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.exception.UserAlreadyRegistered;
import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.RegisterDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.exception.UserNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(RegisterDto dto) {

        if(userRepository.findByEmail(dto.email()) != null) {
            throw new UserAlreadyRegistered("User already registered.");
        }

        User newUser = User.builder()
                .email(dto.email())
                .fullName(dto.name())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(newUser);

    }

    public void updateUserRole(UUID userId, Role newRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setRole(newRole);
        userRepository.save(user);

    }



}
