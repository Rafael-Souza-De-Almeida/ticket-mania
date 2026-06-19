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
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

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

        @Test
        @DisplayName("Should return 201 Created and the created event")
        void shouldCreateAnEventAndReturn201() throws Exception {

            LocalDateTime eventDate = LocalDateTime.of(2026, 6, 24, 20, 0, 0);
            UUID generatedId = UUID.randomUUID();

            Event eventMock = new Event();
            eventMock.setId(generatedId);
            eventMock.setName("Flamengo x São Paulo");
            eventMock.setPlace("Maracanã");
            eventMock.setCapacity(65000);
            eventMock.setDate(eventDate);

            EventResponseDto responseDto = new EventResponseDto(eventMock);

            Event requestPayload = new Event();
            requestPayload.setName("Flamengo x São Paulo");
            requestPayload.setPlace("Maracanã");
            requestPayload.setCapacity(65000);
            requestPayload.setDate(eventDate);


            Mockito.when(eventService.create(Mockito.any())).thenReturn(responseDto);

            mockMvc.perform(post("/api/v1/event")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestPayload)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Flamengo x São Paulo"))
                    .andExpect(jsonPath("$.date").value("2026-06-24T20:00:00"))
                    .andExpect(jsonPath("$.place").value("Maracanã"))
                    .andExpect(jsonPath("$.capacity").value(65000));
        }
}