-- SmartCity360 schema and sample data (MySQL 8+).
-- Safe to re-run: it recreates the database from scratch.

DROP DATABASE IF EXISTS smartcity360;
CREATE DATABASE smartcity360 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartcity360;

CREATE TABLE city_zone (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    parent_id  BIGINT       NULL,
    usage_kwh  DOUBLE       NOT NULL DEFAULT 0.0,
    power_on   BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_city_zone_parent FOREIGN KEY (parent_id) REFERENCES city_zone (id),
    CONSTRAINT ck_city_zone_type CHECK (type IN ('CITY', 'DISTRICT', 'STREET', 'BUILDING'))
);

-- Backwards compatibility view
CREATE OR REPLACE VIEW zones AS SELECT * FROM city_zone;

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

CREATE TABLE street_light_type (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(80)  NOT NULL UNIQUE,
    model           VARCHAR(100) NOT NULL,
    wattage         INT          NOT NULL,
    colour          VARCHAR(30)  NOT NULL,
    dimming_profile VARCHAR(255) NOT NULL
);

CREATE TABLE street_light (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    code     VARCHAR(80)  NOT NULL UNIQUE,
    location VARCHAR(200) NOT NULL,
    type_id  BIGINT       NOT NULL,
    faulty   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_street_light_type FOREIGN KEY (type_id) REFERENCES street_light_type (id)
);

-- Backwards compatibility view for legacy streetlights
CREATE OR REPLACE VIEW streetlights AS
SELECT sl.id, sl.code, sl.location, slt.model, slt.wattage, slt.colour, NOT sl.faulty AS active
FROM street_light sl
JOIN street_light_type slt ON sl.type_id = slt.id;

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

CREATE TABLE policy_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_name VARCHAR(150)  NOT NULL,
    old_rule    VARCHAR(1000) NOT NULL,
    new_rule    VARCHAR(1000) NOT NULL,
    changed_at  DATETIME      NOT NULL
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
-- Part B: Smart zone hierarchy (Composite pattern, table city_zone)
INSERT INTO city_zone (id, name, type, parent_id, usage_kwh, power_on) VALUES
    (1,  'Colombo',                       'CITY',     NULL, 0.00,   TRUE),
    (2,  'Colombo 03 - Kollupitiya',      'DISTRICT', 1,    0.00,   TRUE),
    (3,  'Galle Road',                    'STREET',   2,    0.00,   TRUE),
    (4,  'Shopping Mall',                 'BUILDING', 3,    450.00, TRUE),
    (5,  'City Hospital',                 'BUILDING', 3,    380.50, TRUE),
    (6,  'Dharmapala Mawatha',            'STREET',   2,    0.00,   TRUE),
    (7,  'Apartment Block A',             'BUILDING', 6,    120.00, TRUE),
    (8,  'Supermarket',                   'BUILDING', 6,    95.25,  TRUE),
    (9,  'Colombo 07 - Cinnamon Gardens', 'DISTRICT', 1,    0.00,   TRUE),
    (10, 'Ward Place',                    'STREET',   9,    0.00,   TRUE),
    (11, 'Public Library',                'BUILDING', 10,   150.00, TRUE),
    (12, 'Town Hall',                     'BUILDING', 10,   210.00, TRUE);

-- Part D: Smart streetlights (Flyweight pattern, tables street_light_type + street_light)
INSERT INTO street_light_type (id, code, model, wattage, colour, dimming_profile) VALUES
    (1, 'URBAN-HIGHWAY-150', 'URBAN-HIGHWAY-150', 150, 'Cool White 5700K', '50,50,50,50,50,50,0,0,0,0,0,0,0,0,0,0,0,0,70,90,100,100,100,100'),
    (2, 'SMART-COMMUNITY-70', 'SMART-COMMUNITY-70',  70, 'Warm Amber 3000K', '30,30,30,30,30,30,0,0,0,0,0,0,0,0,0,0,0,0,50,70,80,80,80,80');

-- Specific sample lights matching UI telemetry table
INSERT INTO street_light (id, code, location, type_id, faulty) VALUES
    (1, 'EXP-001',     '6.9147, 79.8489', 1, FALSE),
    (2, 'EXP-002',     '6.9152, 79.8491', 1, FALSE),
    (3, 'ART-00000',   '6.8500, 79.8400', 1, TRUE),
    (4, 'RES-7001',    '6.8500, 79.8410', 2, FALSE),
    (5, 'RES-7002',    '6.8500, 79.8420', 2, FALSE),
    (6, 'RES-7003',    '6.8500, 79.8430', 2, FALSE);

-- Procedure to generate the remainder of the 10,004 city streetlights
DELIMITER //
CREATE PROCEDURE seed_street_lights()
BEGIN
    DECLARE i INT DEFAULT 1;
    -- Remaining 999 arterial fixtures (URBAN-HIGHWAY-150) -> total 1,002 main-road
    WHILE i < 1000 DO
        INSERT INTO street_light (code, location, type_id, faulty)
        VALUES (CONCAT('ART-', LPAD(i, 5, '0')), CONCAT('6.8500, 79.84', (i % 90)), 1, FALSE);
        SET i = i + 1;
    END WHILE;
    -- Remaining 8,999 community luminaires (SMART-COMMUNITY-70) -> total 9,002 residential (24 faulty -> 25 total faulty)
    SET i = 4;
    WHILE i <= 9002 DO
        INSERT INTO street_light (code, location, type_id, faulty)
        VALUES (CONCAT('RES-', LPAD(7000 + i, 5, '0')), CONCAT('6.8500, 79.84', (i % 90)), 2, (i <= 27));
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;
CALL seed_street_lights();
DROP PROCEDURE seed_street_lights;

-- Part E: City policies (Interpreter pattern, table city_policies)
INSERT INTO city_policies (id, name, expression, enabled) VALUES
    (1, 'Night Quiet Zone Regulation',     '(hour >= 23 OR hour < 6) AND soundLevel <= 45 OR ((hour >= 6 AND hour < 23) AND soundLevel <= 65)', TRUE),
    (2, 'Arterial Commercial Access',      'transitPass == 1 OR hour >= 8 AND hour < 19', TRUE),
    (3, 'Low Emission Transit Corridor',   'emissionTier >= 4 OR NOT (hour >= 7 AND hour < 10)', TRUE);

INSERT INTO energy_readings (meter_code, kilowatt_hours, recorded_at) VALUES
    ('MTR-001', 12.5, NOW());

INSERT INTO complaints (resident_name, description, severity, status, assigned_level, created_at) VALUES
    ('A. Perera', 'Streetlight flickering near the bus stop', 2, 'ASSIGNED', 'LOCAL', NOW());
