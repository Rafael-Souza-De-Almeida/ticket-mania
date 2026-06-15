package com.github.rafael_souza_de_almeida.ticket_mania.repository;

import com.github.rafael_souza_de_almeida.ticket_mania.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
}
