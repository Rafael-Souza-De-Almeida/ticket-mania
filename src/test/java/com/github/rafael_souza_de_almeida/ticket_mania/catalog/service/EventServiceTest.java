package com.github.rafael_souza_de_almeida.ticket_mania.catalog.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private static final String EVENT_NAME = "Flamengo x São Paulo";
    private static final String EVENT_PLACE = "Maracanã";
    private static final int EVENT_CAPACITY = 65000;
    private static final LocalDateTime EVENT_DATE = LocalDateTime.now().plusMonths(6);

    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id(UUID.randomUUID())
                .name(EVENT_NAME)
                .place(EVENT_PLACE)
                .capacity(EVENT_CAPACITY)
                .date(EVENT_DATE)
                .build();
    }

    @Test
    @DisplayName("findAll - should return a list with all events mapped to EventResponseDto")
    void shouldReturnAllEventsMapped() {
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<EventResponseDto> result = eventService.findAll();

        assertEquals(1, result.size());
        assertEquals(event.getId(), result.get(0).getId());
        assertEquals(EVENT_NAME, result.get(0).getName());
        assertEquals(EVENT_PLACE, result.get(0).getPlace());
        assertEquals(EVENT_CAPACITY, result.get(0).getCapacity());
        assertEquals(EVENT_DATE, result.get(0).getDate());
    }

    @Test
    @DisplayName("findAll - should return an empty list when there are no events")
    void shouldReturnEmptyListWhenNoEvents() {
        when(eventRepository.findAll()).thenReturn(List.of());

        List<EventResponseDto> result = eventService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("create - should build and persist a new Event, returning the mapped response")
    void shouldCreateAndPersistEvent() throws Exception {
        EventRequestDto requestDto = buildRequestDto();

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        EventResponseDto response = eventService.create(requestDto);

        verify(eventRepository, times(1)).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertEquals(EVENT_NAME, savedEvent.getName());
        assertEquals(EVENT_PLACE, savedEvent.getPlace());
        assertEquals(EVENT_CAPACITY, savedEvent.getCapacity());
        assertEquals(EVENT_DATE, savedEvent.getDate());

        assertEquals(EVENT_NAME, response.getName());
        assertEquals(EVENT_PLACE, response.getPlace());
        assertEquals(EVENT_CAPACITY, response.getCapacity());
        assertEquals(EVENT_DATE, response.getDate());
    }

    private EventRequestDto buildRequestDto() throws Exception {
        EventRequestDto dto = EventRequestDto.class
                .getDeclaredConstructor()
                .newInstance();

        setField(dto, "name", EVENT_NAME);
        setField(dto, "place", EVENT_PLACE);
        setField(dto, "capacity", EVENT_CAPACITY);
        setField(dto, "date", EVENT_DATE);

        return dto;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
