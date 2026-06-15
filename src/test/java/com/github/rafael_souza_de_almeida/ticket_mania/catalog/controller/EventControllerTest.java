package com.github.rafael_souza_de_almeida.ticket_mania.catalog.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    @DisplayName("Should return 200 and an event list")
    void shouldReturnAListWithAllEvents() throws Exception {

        LocalDateTime eventDate = LocalDateTime.of(
                2026, 6, 24, 20, 0, 0);

        Event eventMock = new Event();
        eventMock.setId(UUID.randomUUID());
        eventMock.setName("Flamengo x São Paulo");
        eventMock.setPlace("Maracanã");
        eventMock.setCapacity(65000);
        eventMock.setDate(eventDate);

        EventResponseDto eventDto = new EventResponseDto(eventMock);

        Mockito.when(eventService.findAll()).thenReturn(List.of(eventDto));


        mockMvc.perform(get("/api/v1/event")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Flamengo x São Paulo"))
                .andExpect(jsonPath("$[0].date").value("2026-06-24T20:00:00"))
                .andExpect(jsonPath("$[0].place").value("Maracanã"))
                .andExpect(jsonPath("$[0].capacity").value(65000));

    }
}