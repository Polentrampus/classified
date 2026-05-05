--liquibase formatted sql
--changeset polentrampus:3

INSERT INTO role (name) VALUES
    ('ROLE_USER'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

-- Добавляем тестового администратора (пароль: admin123)
INSERT INTO "user" (name, last_name, role_id, email, phone, password) 
VALUES (
    'System',
    'Administrator',
    (SELECT id FROM role WHERE name = 'ROLE_ADMIN'),
    'admin@marketplace.local',
    '+79000000001',
    '$2a$10$rBV2JDeWW3.VTkQKQOZ6.Oy0xL5zZ.D9x5i5JWH7tqL0Y8XKcR9y'
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

