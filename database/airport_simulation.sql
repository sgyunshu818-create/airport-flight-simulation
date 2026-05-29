CREATE DATABASE IF NOT EXISTS airport_simulation
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE airport_simulation;

DROP TABLE IF EXISTS simulation_record;
DROP TABLE IF EXISTS simulation_task;
DROP TABLE IF EXISTS gate;
DROP TABLE IF EXISTS runway;
DROP TABLE IF EXISTS flight;

CREATE TABLE flight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flight_no VARCHAR(32) NOT NULL UNIQUE,
    airline VARCHAR(100),
    departure_airport VARCHAR(32),
    arrival_airport VARCHAR(32),
    flight_type VARCHAR(32) NOT NULL,
    aircraft_type VARCHAR(32) NOT NULL,
    planned_departure_time DATETIME,
    planned_arrival_time DATETIME,
    actual_departure_time DATETIME,
    actual_arrival_time DATETIME,
    status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED',
    delay_minutes INT NOT NULL DEFAULT 0,
    priority INT NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE runway (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    runway_no VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'IDLE',
    support_type VARCHAR(32) NOT NULL DEFAULT 'BOTH',
    current_flight_id BIGINT,
    occupied_start_time DATETIME,
    occupied_end_time DATETIME,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE gate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gate_no VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'IDLE',
    aircraft_type VARCHAR(32) NOT NULL,
    current_flight_id BIGINT,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE simulation_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    time_step_minutes INT NOT NULL,
    current_time DATETIME,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE simulation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    flight_id BIGINT,
    event_time DATETIME NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_simulation_record_task_time (task_id, event_time, id),
    INDEX idx_simulation_record_flight (flight_id)
);

INSERT INTO runway (runway_no, status, support_type) VALUES
('R01', 'IDLE', 'BOTH'),
('R02', 'IDLE', 'BOTH');

INSERT INTO gate (gate_no, status, aircraft_type) VALUES
('G01', 'IDLE', 'SMALL'),
('G02', 'IDLE', 'MEDIUM'),
('G03', 'IDLE', 'LARGE');

INSERT INTO flight (
    flight_no,
    airline,
    departure_airport,
    arrival_airport,
    flight_type,
    aircraft_type,
    planned_departure_time,
    planned_arrival_time,
    status,
    delay_minutes,
    priority
) VALUES
('MU5101', 'China Eastern', 'SHA', 'PEK', 'DEPARTURE', 'MEDIUM', '2026-06-01 08:00:00', NULL, 'SCHEDULED', 0, 1),
('CA1202', 'Air China', 'CAN', 'SHA', 'ARRIVAL', 'MEDIUM', NULL, '2026-06-01 08:00:00', 'SCHEDULED', 0, 2),
('CZ3305', 'China Southern', 'SHA', 'SZX', 'DEPARTURE', 'LARGE', '2026-06-01 08:05:00', NULL, 'SCHEDULED', 0, 1),
('HO1258', 'Juneyao Air', 'CTU', 'SHA', 'ARRIVAL', 'SMALL', NULL, '2026-06-01 08:05:00', 'SCHEDULED', 0, 1),
('FM9231', 'Shanghai Airlines', 'SHA', 'XMN', 'DEPARTURE', 'SMALL', '2026-06-01 08:10:00', NULL, 'SCHEDULED', 0, 0),
('ZH9108', 'Shenzhen Airlines', 'SZX', 'SHA', 'ARRIVAL', 'LARGE', NULL, '2026-06-01 08:10:00', 'SCHEDULED', 0, 2),
('MF8506', 'Xiamen Air', 'SHA', 'CKG', 'DEPARTURE', 'MEDIUM', '2026-06-01 08:15:00', NULL, 'SCHEDULED', 0, 0),
('3U8962', 'Sichuan Airlines', 'KMG', 'SHA', 'ARRIVAL', 'MEDIUM', NULL, '2026-06-01 08:15:00', 'SCHEDULED', 0, 1);
