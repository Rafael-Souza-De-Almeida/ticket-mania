package com.github.rafael_souza_de_almeida.ticket_mania.catalog.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.service.EventService;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private TicketService ticketService;

    private static final String EVENT_NAME = "Flamengo x São Paulo";
    private static final String EVENT_PLACE = "Maracanã";
    private static final int EVENT_CAPACITY = 65000;
    private static final LocalDateTime EVENT_DATE = LocalDateTime.of(2027, 6, 24, 20, 0, 0);
    private static final String API_URL = "/api/v1/event";

    private UUID eventId;
    private EventResponseDto eventResponseDto;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        eventResponseDto = new EventResponseDto(buildEvent(eventId));
    }

    @Test
    @DisplayName("GET /api/v1/event - should return 200 and the event list")
    void shouldReturnAListWithAllEvents() throws Exception {
        Mockito.when(eventService.findAll()).thenReturn(List.of(eventResponseDto));

        mockMvc.perform(get(API_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$[0].name").value(EVENT_NAME))
                .andExpect(jsonPath("$[0].date").value("2027-06-24T20:00:00"))
                .andExpect(jsonPath("$[0].place").value(EVENT_PLACE))
                .andExpect(jsonPath("$[0].capacity").value(EVENT_CAPACITY));
    }

    @Test
    @DisplayName("POST /api/v1/event - should return 201 and event created")
    void shouldCreateAnEventAndReturn201() throws Exception {
        EventRequestDto requestPayload = buildEventRequest();

        Mockito.when(eventService.create(Mockito.any(EventRequestDto.class))).thenReturn(eventResponseDto);

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.name").value(EVENT_NAME))
                .andExpect(jsonPath("$.date").value("2027-06-24T20:00:00"))
                .andExpect(jsonPath("$.place").value(EVENT_PLACE))
                .andExpect(jsonPath("$.capacity").value(EVENT_CAPACITY));
    }

    @Test
    @DisplayName("POST /api/v1/event - should return 400 when body is invalid")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private Event buildEvent(UUID id) {
        Event event = new Event();
        event.setId(id);
        event.setName(EVENT_NAME);
        event.setPlace(EVENT_PLACE);
        event.setCapacity(EVENT_CAPACITY);
        event.setDate(EVENT_DATE);
        return event;
    }

    private EventRequestDto buildEventRequest() throws Exception {
        String json = String.format("""
                {
                  "name": "%s",
                  "place": "%s",
                  "capacity": %d,
                  "date": "%s"
                }
                """, EVENT_NAME, EVENT_PLACE, EVENT_CAPACITY, EVENT_DATE);

        return objectMapper.readValue(json, EventRequestDto.class);
    }
}