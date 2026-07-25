package com.ticketing.inventory;

import com.ticketing.inventory.entity.Seat;
import com.ticketing.inventory.entity.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventId(Long id);

    long countByEventIdAndStatus(Long eventId, SeatStatus status);
}
