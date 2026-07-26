ALTER TABLE seats
    ADD COLUMN booking_reference VARCHAR(64);

CREATE INDEX idx_seats_booking_reference ON seats (booking_reference);