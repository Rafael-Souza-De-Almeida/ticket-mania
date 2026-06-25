package com.github.rafael_souza_de_almeida.ticket_mania.order.repository;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
}
