package com.events.modules.cart.service.cartitem;

import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.cart.dto.UpdateCartItemDto;
import com.events.modules.cart.entity.CartItem;
import com.events.modules.cart.exception.CartItemNotFoundException;
import com.events.modules.cart.repository.ICartItemRepository;
import com.events.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CartItemService implements ICartItemService {

    private final ICartItemRepository cartItemRepository;
    private final IAuthService authService;

    @Override
    public void deleteItem(UUID itemId) {
        User currentUser  = authService.getCurrentUser();
        cartItemRepository.findByItemIdAndUserId(itemId, currentUser.getId())
                .orElseThrow( () -> new CartItemNotFoundException(itemId));

        cartItemRepository.deleteById(itemId);

    }

    @Override
    public void updateCartItem(UpdateCartItemDto itemDto) {
        User currentUser  = authService.getCurrentUser();
        CartItem cartItem = cartItemRepository.findByItemIdAndUserId(itemDto.cartItemId(), currentUser.getId())
                .orElseThrow( () -> new CartItemNotFoundException(itemDto.cartItemId()));

        cartItem.updateQuantity(itemDto.newQuantity());

        cartItemRepository.save(cartItem);
    }
}
