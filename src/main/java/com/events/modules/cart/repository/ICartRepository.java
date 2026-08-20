package com.events.modules.cart.repository;

import com.events.modules.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ICartRepository extends JpaRepository<Cart, UUID> {

    @Query("SELECT ca FROM Cart ca LEFT JOIN FETCH ca.items ci WHERE ca.userId =: userId")
    Optional<Cart> findByUserId(@Param("userId") UUID userId);


    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem ca")
    int deleteCartItem(@Param("itemId") UUID itemId, @Param("userId") UUID userId);
}
