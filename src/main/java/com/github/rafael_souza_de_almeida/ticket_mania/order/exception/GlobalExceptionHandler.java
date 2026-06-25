package com.github.rafael_souza_de_almeida.ticket_mania.order.exception;

import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleTicketUnavailable(TicketUnavailableException ex) {

        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> eventNotFoundException(EventNotFoundException ex) {

        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}