package com.events.modules.event.controller;

import com.events.TestContexts.controllers.ControllerTestContext;
import com.events.modules.event.service.IEventService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EventControllerTest extends ControllerTestContext {

    @MockitoBean
    private IEventService eventService;

    @Test
    void testCancelEvent() throws Exception {

        UUID eventId = UUID.randomUUID();
        doNothing().when(eventService).cancelEvent(eventId);

        mockMvc.perform(patch("/api/v1/events/{id}/cancel", eventId))
                .andExpect(status().isOk());
    }

    @Test
    void testPublishEvent() throws Exception {

        UUID eventId = UUID.randomUUID();
        doNothing().when(eventService).publishEvent(eventId);

        mockMvc.perform(patch("/api/v1/events/{id}/publish", eventId))
                .andExpect(status().isOk());
    }
}
