-- ============================================================
-- CarAfford AI – MySQL Schema + Seed Data
-- Database: carafford_db
-- MySQL Version: 8.0+
-- ============================================================

-- Create and use the database
CREATE DATABASE IF NOT EXISTS carafford_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE carafford_db;

-- ─── DROP TABLES (clean slate for re-run) ───────────────────
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS affordability_reports;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS loan_options;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- ─── USERS TABLE ─────────────────────────────────────────────
CREATE TABLE users (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    password_hash  VARCHAR(100) NULL,
    monthly_income DOUBLE       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX idx_users_email (email ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Registered users of CarAfford AI';

-- ─── EXPENSES TABLE ──────────────────────────────────────────
CREATE TABLE expenses (
    id                    BIGINT  NOT NULL AUTO_INCREMENT,
    user_id               BIGINT  NOT NULL,
    fixed_expenses        DOUBLE  NOT NULL COMMENT 'Monthly fixed costs: rent, utilities, groceries',
    existing_emi          DOUBLE  NOT NULL COMMENT 'Sum of all existing loan EMIs',
    down_payment          DOUBLE  NOT NULL COMMENT 'Upfront payment for the car',
    preferred_tenure_years INT    NOT NULL COMMENT 'Preferred loan duration: 3, 5, or 7 years',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_expenses_user_id (user_id ASC),
    CONSTRAINT fk_expenses_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User financial obligations and preferences';

-- ─── CARS TABLE ──────────────────────────────────────────────
CREATE TABLE cars (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    brand            VARCHAR(100)  NOT NULL,
    model            VARCHAR(100)  NOT NULL,
    variant          VARCHAR(100)  NULL,
    price            DOUBLE        NOT NULL COMMENT 'On-road price in INR',
    mileage          DOUBLE        NOT NULL COMMENT 'Fuel efficiency in km/L',
    maintenance_cost DOUBLE        NOT NULL COMMENT 'Estimated monthly upkeep in INR',
    fuel_type        VARCHAR(20)   NOT NULL COMMENT 'PETROL | DIESEL | ELECTRIC | HYBRID',
    segment          VARCHAR(50)   NULL     COMMENT 'Hatchback | Sedan | SUV | MicroSUV | Electric',
    description      VARCHAR(500)  NULL,
    PRIMARY KEY (id),
    INDEX idx_cars_price (price ASC),
    INDEX idx_cars_fuel_type (fuel_type ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Car catalog for recommendation engine';

-- ─── LOAN OPTIONS TABLE ──────────────────────────────────────
CREATE TABLE loan_options (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    bank_name     VARCHAR(100)  NULL     COMMENT 'Lender name for future bank comparison',
    interest_rate DOUBLE        NOT NULL COMMENT 'Annual interest rate as percentage',
    tenure_years  INT           NOT NULL COMMENT 'Loan duration in years',
    PRIMARY KEY (id),
    INDEX idx_loan_tenure (tenure_years ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Available loan options by bank and tenure';

-- ─── AFFORDABILITY REPORTS TABLE ─────────────────────────────
CREATE TABLE affordability_reports (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    user_id             BIGINT       NOT NULL,
    max_emi             DOUBLE       NOT NULL COMMENT 'Calculated maximum safe EMI',
    max_car_price       DOUBLE       NOT NULL COMMENT 'Maximum affordable car price',
    stress_score        INT          NOT NULL COMMENT 'Financial stress score 0-100',
    stress_level        VARCHAR(20)  NOT NULL COMMENT 'SAFE | CAUTION | RISKY',
    stress_explanation  VARCHAR(500) NULL,
    recommended_car_id  BIGINT       NULL,
    verdict             VARCHAR(20)  NOT NULL COMMENT 'BUY | DONT_BUY',
    upgrade_advice      VARCHAR(500) NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_report_user_id (user_id ASC),
    INDEX idx_report_created_at (created_at DESC),
    CONSTRAINT fk_report_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_report_car
        FOREIGN KEY (recommended_car_id) REFERENCES cars (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Generated affordability reports';

-- ────────────────────────────────────────────────────────────
-- END OF SCHEMA
-- ────────────────────────────────────────────────────────────


-- ─── VERIFICATION QUERIES ────────────────────────────────────
-- Run these to verify data loaded correctly:
-- SELECT COUNT(*) FROM cars;         -- Expected: 25
-- SELECT COUNT(*) FROM loan_options; -- Expected: 9
-- SELECT brand, model, price FROM cars ORDER BY price;
-- SELECT * FROM loan_options WHERE tenure_years = 5 ORDER BY interest_rate;


