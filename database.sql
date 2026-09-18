SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS disputes;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS offer_responses;
DROP TABLE IF EXISTS offer_negotiations;
DROP TABLE IF EXISTS offers;
DROP TABLE IF EXISTS provider_availability;
DROP TABLE IF EXISTS provider_services;
DROP TABLE IF EXISTS request_members;
DROP TABLE IF EXISTS service_requests;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS service_categories;
DROP TABLE IF EXISTS service_providers;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    phone       VARCHAR(20),
    role        ENUM('CUSTOMER','SERVICE_PROVIDER','ADMIN') NOT NULL,
    display_name VARCHAR(100)   NOT NULL,
    locality    VARCHAR(100),
    is_active   TINYINT(1)      NOT NULL DEFAULT 1,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
);

CREATE TABLE service_providers (
    provider_id  INT           PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    bio          TEXT,
    service_area VARCHAR(255),
    is_verified  TINYINT(1)   NOT NULL DEFAULT 0,
    avg_rating   DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (provider_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE service_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE services (
    service_id  INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    FOREIGN KEY (category_id) REFERENCES service_categories(category_id) ON DELETE CASCADE,
    INDEX idx_services_category (category_id)
);

CREATE TABLE service_requests (
    request_id          INT AUTO_INCREMENT PRIMARY KEY,
    creator_id          INT             NOT NULL,
    service_id          INT             NOT NULL,
    problem_description TEXT            NOT NULL,
    locality            VARCHAR(100)    NOT NULL,
    preferred_date      DATE            NOT NULL,
    preferred_time      TIME            NOT NULL,
    budget_expectation  DECIMAL(10,2),
    max_group_size      INT             NOT NULL DEFAULT 10,
    current_member_count INT            NOT NULL DEFAULT 1,
    status              ENUM('REQUESTED','GROUP_FORMING','PROVIDER_CONTACTED','OFFER_RECEIVED','NEGOTIATING','OFFER_ACCEPTED','BOOKED','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'REQUESTED',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(user_id),
    FOREIGN KEY (service_id) REFERENCES services(service_id),
    INDEX idx_requests_service_locality (service_id, locality),
    INDEX idx_requests_status (status),
    INDEX idx_requests_date (preferred_date)
);

CREATE TABLE request_members (
    member_id       INT AUTO_INCREMENT PRIMARY KEY,
    request_id      INT         NOT NULL,
    user_id         INT         NOT NULL,
    anonymous_alias VARCHAR(20) NOT NULL,
    is_active       TINYINT(1)  NOT NULL DEFAULT 1,
    joined_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_member_request (request_id, user_id),
    FOREIGN KEY (request_id) REFERENCES service_requests(request_id),
    FOREIGN KEY (user_id)    REFERENCES users(user_id),
    INDEX idx_members_request (request_id)
);

CREATE TABLE provider_services (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    provider_id INT NOT NULL,
    service_id  INT NOT NULL,
    UNIQUE KEY uq_provider_service (provider_id, service_id),
    FOREIGN KEY (provider_id) REFERENCES service_providers(provider_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id)  REFERENCES services(service_id) ON DELETE CASCADE
);

CREATE TABLE provider_availability (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    provider_id  INT NOT NULL,
    day_of_week  ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    FOREIGN KEY (provider_id) REFERENCES service_providers(provider_id) ON DELETE CASCADE
);

CREATE TABLE offers (
    offer_id         INT AUTO_INCREMENT PRIMARY KEY,
    request_id       INT             NOT NULL,
    provider_id      INT             NOT NULL,
    amount_per_member DECIMAL(10,2)  NOT NULL,
    required_members INT             NOT NULL DEFAULT 1,
    valid_until      TIMESTAMP       NOT NULL,
    status           ENUM('PENDING','COUNTERED','ACCEPTED','REJECTED','EXPIRED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id)  REFERENCES service_requests(request_id),
    FOREIGN KEY (provider_id) REFERENCES service_providers(provider_id),
    INDEX idx_offers_request (request_id),
    INDEX idx_offers_status (status),
    INDEX idx_offers_expiry (valid_until, status)
);

CREATE TABLE offer_negotiations (
    negotiation_id INT AUTO_INCREMENT PRIMARY KEY,
    offer_id       INT             NOT NULL,
    sender_role    ENUM('PROVIDER','COORDINATOR') NOT NULL,
    amount         DECIMAL(10,2)   NOT NULL,
    message        TEXT,
    created_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (offer_id) REFERENCES offers(offer_id) ON DELETE CASCADE,
    INDEX idx_negotiations_offer (offer_id)
);

CREATE TABLE offer_responses (
    response_id  INT AUTO_INCREMENT PRIMARY KEY,
    offer_id     INT NOT NULL,
    member_id    INT NOT NULL,
    status       ENUM('PENDING','ACCEPTED','DECLINED') NOT NULL DEFAULT 'PENDING',
    responded_at TIMESTAMP,
    UNIQUE KEY uq_offer_member (offer_id, member_id),
    FOREIGN KEY (offer_id)  REFERENCES offers(offer_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES request_members(member_id)
);

CREATE TABLE bookings (
    booking_id     INT AUTO_INCREMENT PRIMARY KEY,
    request_id     INT  NOT NULL,
    offer_id       INT  NOT NULL UNIQUE,
    provider_id    INT  NOT NULL,
    scheduled_date DATE,
    scheduled_time TIME,
    status         ENUM('CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id)  REFERENCES service_requests(request_id),
    FOREIGN KEY (offer_id)    REFERENCES offers(offer_id),
    FOREIGN KEY (provider_id) REFERENCES service_providers(provider_id),
    INDEX idx_bookings_provider (provider_id),
    INDEX idx_bookings_status (status)
);

CREATE TABLE chat_messages (
    message_id   INT AUTO_INCREMENT PRIMARY KEY,
    request_id   INT         NOT NULL,
    sender_alias VARCHAR(30) NOT NULL,
    content      TEXT        NOT NULL,
    sent_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES service_requests(request_id),
    INDEX idx_chat_request (request_id, sent_at)
);

CREATE TABLE reviews (
    review_id   INT AUTO_INCREMENT PRIMARY KEY,
    booking_id  INT NOT NULL,
    reviewer_id INT NOT NULL,
    provider_id INT NOT NULL,
    rating      INT NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_booking_reviewer (booking_id, reviewer_id),
    CHECK (rating BETWEEN 1 AND 5),
    FOREIGN KEY (booking_id)  REFERENCES bookings(booking_id),
    FOREIGN KEY (reviewer_id) REFERENCES users(user_id),
    FOREIGN KEY (provider_id) REFERENCES service_providers(provider_id),
    INDEX idx_reviews_provider (provider_id)
);

CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT          NOT NULL,
    message         VARCHAR(500) NOT NULL,
    is_read         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id, is_read)
);

CREATE TABLE reports (
    report_id   INT AUTO_INCREMENT PRIMARY KEY,
    reporter_id INT  NOT NULL,
    target_type ENUM('USER','PROVIDER','REQUEST') NOT NULL,
    target_id   INT  NOT NULL,
    reason      TEXT NOT NULL,
    status      ENUM('OPEN','UNDER_REVIEW','RESOLVED','DISMISSED') NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(user_id),
    INDEX idx_reports_status (status)
);

CREATE TABLE disputes (
    dispute_id  INT AUTO_INCREMENT PRIMARY KEY,
    booking_id  INT  NOT NULL,
    filed_by    INT  NOT NULL,
    description TEXT NOT NULL,
    status      ENUM('OPEN','UNDER_REVIEW','RESOLVED','DISMISSED') NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    FOREIGN KEY (filed_by)   REFERENCES users(user_id)
);

INSERT INTO users (email, password_hash, phone, role, display_name, locality, is_active) VALUES
('admin@sameneed.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewLNW4j2ZTOdKbM2', '9800000001', 'ADMIN', 'Admin', NULL, 1),
('alice@example.com',  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewLNW4j2ZTOdKbM2', '9800000002', 'CUSTOMER', 'Alice Sharma', 'Koregaon Park', 1),
('bob@example.com',    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewLNW4j2ZTOdKbM2', '9800000003', 'CUSTOMER', 'Bob Verma', 'Koregaon Park', 1),
('carol@example.com',  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewLNW4j2ZTOdKbM2', '9800000004', 'CUSTOMER', 'Carol Singh', 'Koregaon Park', 1),
('dave@example.com',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewLNW4j2ZTOdKbM2', '9800000005', 'CUSTOMER', 'Dave Mehta', 'Aundh', 1),
('fix@provider.com',   '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/lewLNW4j2ZTOdKbM2', '9800000006', 'SERVICE_PROVIDER', 'FixIt Pro', NULL, 1);

INSERT INTO service_providers (provider_id, business_name, bio, service_area, is_verified, avg_rating) VALUES
(6, 'FixIt Pro Services', 'Expert in home appliance repairs and plumbing with 8 years experience.', 'Koregaon Park, Aundh, Baner', 1, 4.30);

INSERT INTO service_categories (name, description) VALUES
('Home Appliances', 'Repair and maintenance of household appliances'),
('Plumbing', 'All plumbing installations and repairs'),
('Electrical', 'Electrical wiring, fitting, and repairs'),
('Carpentry', 'Furniture, doors, windows, and woodwork'),
('Painting', 'Interior and exterior painting services'),
('Cleaning', 'Deep cleaning and housekeeping services'),
('AC & HVAC', 'Air conditioning installation, service, and repair'),
('Pest Control', 'Treatment for all types of pest infestations');

INSERT INTO services (category_id, name, description) VALUES
(1, 'Washing Machine Repair', 'Diagnosis and repair of all washing machine brands and types'),
(1, 'Refrigerator Repair', 'Compressor, cooling, and general fridge repairs'),
(1, 'Microwave Repair', 'Heating element, door, and electronics repair'),
(1, 'Dishwasher Repair', 'Motor, pump, and drainage system repairs'),
(2, 'Pipe Leak Repair', 'Detection and fixing of pipe leaks'),
(2, 'Tap Replacement', 'Replacing kitchen and bathroom taps'),
(2, 'Drain Cleaning', 'Clearing blocked drains and pipes'),
(3, 'Fan Installation', 'Ceiling and exhaust fan fitting'),
(3, 'Switchboard Repair', 'Faulty switchboard and socket repair'),
(3, 'Wiring Inspection', 'Full home electrical safety inspection'),
(7, 'AC Service', 'General AC servicing and gas refill'),
(7, 'AC Repair', 'Fault diagnosis and AC component repair'),
(7, 'AC Installation', 'New AC unit installation and testing'),
(8, 'General Pest Control', 'Cockroach, ant, and rodent treatment'),
(8, 'Termite Treatment', 'Anti-termite chemical treatment');

INSERT INTO provider_services (provider_id, service_id) VALUES
(6, 1), (6, 2), (6, 3), (6, 5), (6, 6), (6, 7), (6, 11), (6, 12);

INSERT INTO service_requests (creator_id, service_id, problem_description, locality, preferred_date, preferred_time, budget_expectation, max_group_size, current_member_count, status) VALUES
(2, 1, 'Washing machine is not draining after the wash cycle completes.', 'Koregaon Park', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', 500.00, 10, 3, 'GROUP_FORMING'),
(3, 1, 'Water is leaking from the bottom of the washing machine.', 'Koregaon Park', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', 600.00, 10, 3, 'GROUP_FORMING');

INSERT INTO request_members (request_id, user_id, anonymous_alias, is_active) VALUES
(1, 2, 'Group Creator', 1),
(1, 3, 'Member 02', 1),
(1, 4, 'Member 03', 1),
(2, 3, 'Group Creator', 1),
(2, 2, 'Member 02', 1),
(2, 4, 'Member 03', 1);
