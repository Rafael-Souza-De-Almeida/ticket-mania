package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.TicketUnavailableException;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private OrderTransactionalService orderTransactionalService;

    @Mock
    private RLock rLock;

    @InjectMocks
    private OrderService orderService;

    private UUID ticketId;
    private User loggedUser;
    private OrderRequestDto requestDto;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        requestDto = new OrderRequestDto(ticketId);
        loggedUser = User.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .role(Role.ROLE_USER)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Test
    @DisplayName("create - should acquire the lock, delegate to the transactional service and release the lock")
    void shouldCreateOrderWhenLockIsAcquired() throws InterruptedException {
        authenticateAs(loggedUser);

        OrderResponseDto expectedResponse = new OrderResponseDto(UUID.randomUUID(), ticketId, loggedUser.getId(), "secret");

        when(redissonClient.getLock("lock:ticket:" + ticketId)).thenReturn(rLock);
        when(rLock.tryLock(1, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(orderTransactionalService.createWithTransaction(requestDto, loggedUser.getId())).thenReturn(expectedResponse);

        OrderResponseDto result = orderService.create(requestDto);

        assertEquals(expectedResponse, result);
        verify(orderTransactionalService, times(1)).createWithTransaction(requestDto, loggedUser.getId());
        verify(rLock, times(1)).unlock();
    }

    @Test
    @DisplayName("create - should throw InsufficientAuthenticationException when there is no authenticated user")
    void shouldThrowWhenUserIsNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(InsufficientAuthenticationException.class, () -> orderService.create(requestDto));
    }

    @Test
    @DisplayName("create - should throw TicketUnavailableException when the lock cannot be acquired")
    void shouldThrowWhenLockIsNotAcquired() throws InterruptedException {
        authenticateAs(loggedUser);

        when(redissonClient.getLock("lock:ticket:" + ticketId)).thenReturn(rLock);
        when(rLock.tryLock(1, 10, TimeUnit.SECONDS)).thenReturn(false);
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        assertThrows(TicketUnavailableException.class, () -> orderService.create(requestDto));

        verify(orderTransactionalService, times(0)).createWithTransaction(requestDto, loggedUser.getId());
        verify(rLock, times(0)).unlock();
    }

    @Test
    @DisplayName("create - should wrap InterruptedException in a RuntimeException and restore the interrupt flag")
    void shouldWrapInterruptedExceptionAndRestoreFlag() throws InterruptedException {
        authenticateAs(loggedUser);

        when(redissonClient.getLock("lock:ticket:" + ticketId)).thenReturn(rLock);
        when(rLock.tryLock(1, 10, TimeUnit.SECONDS)).thenThrow(new InterruptedException("interrupted"));
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        assertThrows(RuntimeException.class, () -> orderService.create(requestDto));

        assertEquals(true, Thread.interrupted(), "Interrupt flag should have been restored on the current thread");
    }
}
