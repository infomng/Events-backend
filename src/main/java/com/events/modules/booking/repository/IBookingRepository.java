package com.events.modules.booking.repository;

import com.events.modules.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IBookingRepository extends JpaRepository<Booking, UUID> {
}
