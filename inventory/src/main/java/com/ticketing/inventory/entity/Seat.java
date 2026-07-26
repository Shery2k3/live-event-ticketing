package com.ticketing.inventory.entity;

import com.ticketing.inventory.entity.base.BaseEntity;
import com.ticketing.inventory.entity.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_seat_per_event",
                columnNames = {"event_id", "seat_number"}
        )
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(name = "booking_reference", length = 64)
    private String bookingReference;
}
