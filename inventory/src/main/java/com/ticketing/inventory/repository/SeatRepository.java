package com.ticketing.inventory.repository;

import com.ticketing.inventory.entity.Seat;
import com.ticketing.inventory.entity.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventId(Long id);

    long countByEventIdAndStatus(Long eventId, SeatStatus status);

    List<Seat> findByBookingReference(String bookingReference);

    /**
     * Locks the requested seat rows FOR UPDATE before we read them.
     * ORDER BY s.id is deliberate: every caller acquires locks in the same
     * order, which makes deadlocks between two overlapping bookings impossible.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :ids order by s.id")
    List<Seat> lockSeatsForUpdate(@Param("ids") List<Long> ids);
}
