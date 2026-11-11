-- SmartCity360 schema and sample data (MySQL 8+).
-- Safe to re-run: it recreates the database from scratch.

DROP DATABASE IF EXISTS smartcity360;
CREATE DATABASE smartcity360 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartcity360;

CREATE TABLE zones (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(150) NOT NULL,
    type      VARCHAR(30)  NOT NULL,
    parent_id BIGINT       NULL,
    CONSTRAINT fk_zones_parent FOREIGN KEY (parent_id) REFERENCES zones (id),
    CONSTRAINT ck_zones_type CHECK (type IN ('CITY', 'DISTRICT', 'STREET', 'BUILDING'))
);

CREATE TABLE complaints (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_name  VARCHAR(150)  NOT NULL,
    description    VARCHAR(1000) NOT NULL,
    severity       INT           NOT NULL,
    status         VARCHAR(30)   NOT NULL,
    assigned_level VARCHAR(30)   NOT NULL,
    created_at     DATETIME      NOT NULL,
    CONSTRAINT ck_complaints_severity CHECK (severity BETWEEN 1 AND 5),
    INDEX idx_complaints_status (status)
);

CREATE TABLE streetlights (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    code     VARCHAR(80)  NOT NULL UNIQUE,
    location VARCHAR(200) NOT NULL,
    model    VARCHAR(100) NOT NULL,
    wattage  INT          NOT NULL,
    colour   VARCHAR(30)  NOT NULL,
    active   BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE energy_readings (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    meter_code     VARCHAR(80) NOT NULL,
    kilowatt_hours DOUBLE      NOT NULL,
    recorded_at    DATETIME    NOT NULL,
    INDEX idx_energy_recorded (recorded_at)
);

CREATE TABLE city_policies (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150)  NOT NULL,
    expression VARCHAR(1000) NOT NULL,
    enabled    BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE app_users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(60)  NOT NULL UNIQUE,
    email         VARCHAR(120) NOT NULL UNIQUE,
    full_name     VARCHAR(120) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    salt          VARCHAR(64)  NOT NULL,
    role          VARCHAR(30)  NOT NULL DEFAULT 'CITIZEN',
    created_at    DATETIME     NOT NULL,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
);

-- ---- sample data ----
-- Default test accounts:
--   admin   / admin123   (ADMIN)
--   officer / officer123 (OFFICER)
--   citizen / citizen123 (CITIZEN)
INSERT INTO app_users (username, email, full_name, password_hash, salt, role, created_at) VALUES
    ('admin',   'admin@smartcity360.lk',   'City Administrator', 'b3d7b2814c0d85be67ebba2c27e0d23ac9f63566b87a930d39de48217fdbe576', '102030405060708090a0b0c0d0e0f001', 'ADMIN',   NOW()),
    ('officer', 'officer@smartcity360.lk', 'Operations Officer', 'dc0436e53d0106d8e54bb882c5443138435c22675ed344993e8f012f023642ac', '2030405060708090a0b0c0d0e0f00102', 'OFFICER', NOW()),
    ('citizen', 'citizen@smartcity360.lk', 'Jane Citizen',       '216e2b01a4e76a2815e9476914348c42f97cf30a058253bfe0de70880a09e8d2', '30405060708090a0b0c0d0e0f0010203', 'CITIZEN', NOW());
INSERT INTO zones (name, type, parent_id) VALUES
    ('Metro City',       'CITY',     NULL),
    ('Central District', 'DISTRICT', 1),
    ('Main Street',      'STREET',   2),
    ('Town Hall',        'BUILDING', 3);

INSERT INTO streetlights (code, location, model, wattage, colour, active) VALUES
    ('SL-001', 'Main Street - North', 'LED-Standard', 60, 'white', TRUE),
    ('SL-002', 'Main Street - South', 'LED-Standard', 60, 'white', TRUE),
    ('SL-003', 'Lake Road',           'LED-Warm',     45, 'amber', FALSE);

INSERT INTO city_policies (name, expression, enabled) VALUES
    ('Night Noise Restriction', 'noiseDb > 60 AND hour >= 22', TRUE);

INSERT INTO energy_readings (meter_code, kilowatt_hours, recorded_at) VALUES
    ('MTR-001', 12.5, NOW());

INSERT INTO complaints (resident_name, description, severity, status, assigned_level, created_at) VALUES
    ('A. Perera', 'Streetlight flickering near the bus stop', 2, 'ASSIGNED', 'LOCAL', NOW());
