package com.ticketing.booking.repository;

import com.ticketing.booking.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // This equals left join fetch
    @EntityGraph(attributePaths = "seats")
    Optional<Booking> findByBookingReference(String bookingReference);

    @EntityGraph(attributePaths = "seats")
    List<Booking> findByUserIdOrderByIdDesc(Long userId);
}