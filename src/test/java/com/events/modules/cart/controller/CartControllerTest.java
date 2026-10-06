package com.events.modules.cart.controller;

import com.events.TestContexts.controllers.ControllerTestContext;
import com.events.modules.cart.dto.AddItemToCartDto;
import com.events.modules.cart.service.ICartService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CartController Tests")
class CartControllerTest extends ControllerTestContext {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ICartService cartService;

    @Test
    void createCartShouldReturnSuccess() throws Exception {
        //When & Then
        mockMvc.perform(post("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.value").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
    }

    @Test
    void addItemToCartShouldReturnSuccess() throws Exception {

        //GIVEN
        AddItemToCartDto addItemToCartDto = AddItemToCartDto.builder()
                .eventId(UUID.randomUUID())
                .priceCategoryId(UUID.randomUUID())
                .quantity(1)
                .build();
        willDoNothing().given(cartService).addItemToCart(any(AddItemToCartDto.class));

        //WHEN & THEN
        mockMvc.perform(put("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(addItemToCartDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.value").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));

        then(cartService).should().addItemToCart(any(AddItemToCartDto.class));
    }

    @Test
    void addItemToCartShouldReturnBadRequestWhenInvalidInput() throws Exception {

        //GIVEN
        AddItemToCartDto addItemToCartDto = AddItemToCartDto.builder()
                .eventId(null)
                .priceCategoryId(null)
                .quantity(null)
                .build();

        //WHEN
        mockMvc.perform(put("/api/v1/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(addItemToCartDto)))
                .andExpect(status().isBadRequest());

        //THEN
        then(cartService).should(never()).addItemToCart(any(AddItemToCartDto.class));
    }

}
