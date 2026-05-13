--liquibase formatted sql
--changeset polentrampus:3

INSERT INTO role (name) VALUES
    ('ROLE_USER'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

-- Добавляем тестового администратора (пароль: password123)
INSERT INTO "user" (name, last_name, role_id, email, phone, password)
VALUES (
           'System',
           'Administrator',
           (SELECT id FROM role WHERE name = 'ROLE_ADMIN'),
           'admin1@marketplace.local',
           '+79000000001',
           '$2a$10$wkFeJY7Hjz6O8TaaH.4j.eESrTMEpQe9OrUnhGNAQy8NjXQC3WKZi'
       ) ON CONFLICT (email) DO NOTHING;

-- Добавляем тестового пользователя (пароль: user123)
INSERT INTO "user" (name, last_name,role_id, email, phone, password) 
VALUES (
    'John',
    'Doe',
    (SELECT id FROM role WHERE name = 'ROLE_USER'),
    'user@example.com',
    '+79000000002',
    '$2a$10$8.UnVuG9HHgqUD8jthfDlO7H5Z5z5z5z5z5z5z5z5z5z5z5z5z5z5'
) ON CONFLICT (email) DO NOTHING;

