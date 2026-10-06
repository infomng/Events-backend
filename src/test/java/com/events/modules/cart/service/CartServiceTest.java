package com.events.modules.cart.service;

import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.cart.dto.AddItemToCartDto;
import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.dto.mapper.ICartMapper;
import com.events.modules.cart.entity.Cart;
import com.events.modules.cart.exception.CartNotFoundException;
import com.events.modules.cart.repository.ICartRepository;
import com.events.modules.cart.service.impl.CartService;
import com.events.modules.cart.testdatabuilder.CartTestDataBuilder;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.exception.EventNotFoundException;
import com.events.modules.event.service.IEventService;
import com.events.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Unit tests for {@link CartService}.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private ICartRepository cartRepository;

    @Mock
    private IEventService eventService;

    @Mock
    private IAuthService authService;

    @Mock
    private ICartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private AddItemToCartDto addItemToCartDto;
    private User user;
    private GetEventDto getEventDto;
    private Cart cart;
    private GetCartDto getCartDto;

    /**
     * Sets up test data and mocks before each test execution.
     */
    @BeforeEach
    void setup() {
        addItemToCartDto = CartTestDataBuilder.getAddItemToCartDto();
        user = CartTestDataBuilder.getUser();
        getEventDto = CartTestDataBuilder.getEventDto();
        cart = CartTestDataBuilder.getCart();
        getCartDto = CartTestDataBuilder.getCartDto();
    }

    @Nested
    class AddItemToCartTests {
        /**
         * Tests that an item is successfully added to the user's cart.
         */
        @Test
        void addItemToCartShouldReturnSuccess() {
            // Given
            given(authService.getCurrentUser()).willReturn(user);
            given(eventService.getEventById(addItemToCartDto.eventId())).willReturn(getEventDto);
            given(cartRepository.save(any(Cart.class))).willReturn(cart);
            given(cartRepository.findByUserId(any(UUID.class))).willReturn(Optional.of(cart));

            //When
            cartService.addItemToCart(addItemToCartDto);

            //Then
            then(eventService).should().getEventById(addItemToCartDto.eventId());
            then(cartRepository).should().save(any(Cart.class));
            then(authService).should().getCurrentUser();
        }

        @Test
        void addItemToCartShouldThrowExceptionWhenEventNotFound(){
            //GIVEN
            given(authService.getCurrentUser()).willReturn(user);
            given(cartRepository.findByUserId(any(UUID.class))).willReturn(Optional.of(cart));
            given(eventService.getEventById(addItemToCartDto.eventId())).willThrow(new EventNotFoundException(addItemToCartDto.eventId()));

            //WHEN & THEN
            assertThatThrownBy(() -> cartService.addItemToCart(addItemToCartDto))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessageContaining("Event with id: " + addItemToCartDto.eventId() + " not found.");

            then(eventService).should().getEventById(addItemToCartDto.eventId());
            then(authService).should().getCurrentUser();
            then(cartMapper).shouldHaveNoInteractions();
            then(cartRepository).should(never()).save(any(Cart.class));
        }

        @Test
        void addItemToCartShouldThrowExceptionWhenCartNotFound(){
            //GIVEN
            given(authService.getCurrentUser()).willReturn(user);
            given(cartRepository.findByUserId(any(UUID.class))).willThrow(new CartNotFoundException(user.getId()));

            //WHEN & THEN
            assertThatThrownBy(() -> cartService.addItemToCart(addItemToCartDto))
                    .isInstanceOf(CartNotFoundException.class)
                    .hasMessageContaining("Cart not found for user with id: " + user.getId());

            then(authService).should().getCurrentUser();
            then(cartRepository).should().findByUserId(user.getId());
            then(eventService).shouldHaveNoInteractions();
            then(cartMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    class GetCurrentUserCartTests {
        @Test
        void getCurrentUserCartShouldReturnSuccess() {
            // Given
            given(authService.getCurrentUser()).willReturn(user);
            given(cartRepository.findByUserId(any(UUID.class))).willReturn(Optional.of(cart));
            given(cartMapper.toDto(any(Cart.class))).willReturn(getCartDto); // Mock the mapping to DTO

            // When
            var result = cartService.getCurrentUserCart();

            // Then
            then(authService).should().getCurrentUser();
            then(cartRepository).should().findByUserId(user.getId());
            then(cartMapper).should().toDto(cart);
            assertThat(result).isNotNull();
            assertEquals(getCartDto, result);
        }

        @Test
        void getCurrentUserCartShouldThrowExceptionWhenCartNotFound() {
            // Given
            given(authService.getCurrentUser()).willReturn(user);
            given(cartRepository.findByUserId(any(UUID.class))).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cartService.getCurrentUserCart())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cart not found for user with id: " + user.getId());

            then(authService).should().getCurrentUser();
            then(cartRepository).should().findByUserId(user.getId());
            then(cartMapper).shouldHaveNoInteractions();
        }
    }

}
