package com.github.rafael_souza_de_almeida.ticket_mania.user.config;

import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static java.rmi.server.LogStream.log;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        String adminEmail = "admin@ticketmania.com";

        if(userRepository.findByEmail(adminEmail) == null) {

            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .role(Role.ROLE_ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Admin created successfully!");

        }
    }
}
