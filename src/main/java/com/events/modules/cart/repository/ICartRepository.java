package com.events.modules.cart.repository;

import com.events.modules.cart.entity.Cart;
import com.events.modules.cart.enumeration.CartStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserId(UUID userId);

    Optional<Cart> findByUserIdAndStatus(UUID userId, CartStatusEnum status);
}
