package com.github.rafael_souza_de_almeida.ticket_mania.user.service;

import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.exception.UserAlreadyRegistered;
import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.RegisterDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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



}
