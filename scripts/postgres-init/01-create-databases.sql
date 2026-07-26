-- Runs only on first initialisation of the postgres volume.
-- POSTGRES_USER=ticketing already exists and owns everything created here.
CREATE DATABASE ticketing_event;
CREATE DATABASE ticketing_inventory;
CREATE DATABASE ticketing_booking;
CREATE DATABASE ticketing_payment;
CREATE DATABASE ticketing_notification;
