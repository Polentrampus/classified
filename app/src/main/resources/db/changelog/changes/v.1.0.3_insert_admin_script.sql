--liquibase formatted sql
--changeset polentrampus:5

DELETE FROM "user" WHERE id = 1;
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

