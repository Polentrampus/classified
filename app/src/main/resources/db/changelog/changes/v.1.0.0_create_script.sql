--liquibase formatted sql
--changeset polentrampus:1

-- таблица ролей
create table if not exists "role"(
	id bigserial primary key,
	name varchar(100) not null unique
);

-- таблица пользователей
create table if not exists "user"(
	id bigserial primary key,
	name varchar(50) not null,
	last_name varchar(50) not null,
	role_id bigint not null references role(id), 
	email varchar(200) not null unique,
	phone varchar(20) unique,	
	password varchar(255) not null,
    deleted boolean not null default false,
	created_at timestamp not null default NOW(),
	updated_at timestamp
);

create table if not exists "city"(
    id bigserial primary key,
    name varchar(100) not null unique
);

-- таблица адресов, у пользователя может быть несколько адресов, например, для разных объявлений
create table if not exists "address"(
	id bigserial primary key,
	user_id bigint references "user"(id) on delete cascade,
    city_id bigint references "city"(id) on delete cascade,
	created_at timestamp not null default NOW()
);

-- Таблица основных категорий товаров
CREATE TABLE IF NOT EXISTS ad_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Таблица типов товаров
CREATE TABLE IF NOT EXISTS product_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Таблица типа объявления, связывающая категорию и тип товара
CREATE TABLE IF NOT EXISTS ad_type (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL REFERENCES "product_type"(id),
    category_id BIGINT NOT NULL REFERENCES "ad_category"(id),
    UNIQUE (type_id, category_id)
);

-- таблица объявлений
create table if not exists "ad"(
	id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    ad_type_id BIGINT REFERENCES ad_type(id) ON DELETE SET NULL,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    quantity INT NOT NULL CHECK (quantity >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SOLD', 'BOOKED')),
    seller_id BIGINT NOT NULL REFERENCES "user"(id) on DELETE CASCADE,
    address_id BIGINT REFERENCES address(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- таблица для фото на объявления
CREATE TABLE IF NOT EXISTS ad_image (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL REFERENCES ad(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    is_main BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Таблица рейтингов пользователей
CREATE TABLE IF NOT EXISTS user_rating
(
    user_id BIGINT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE ,
    rating NUMERIC(3,2) NOT NULL DEFAULT 0 CHECK (rating BETWEEN 0 AND 5)
);

-- Таблица чатов
CREATE TABLE IF NOT EXISTS chat (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT REFERENCES ad(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Таблица участников чата (связь M:N)
CREATE TABLE IF NOT EXISTS chat_participant (
    chat_id BIGINT NOT NULL REFERENCES chat(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (chat_id, user_id)
);

-- Таблица сообщений
CREATE TABLE IF NOT EXISTS message (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chat(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES "user"(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Таблица заказов (история продаж)
CREATE TABLE IF NOT EXISTS "order" (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL REFERENCES ad(id),
    buyer_id BIGINT NOT NULL REFERENCES "user"(id),
    seller_id BIGINT NOT NULL REFERENCES "user"(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    total_price NUMERIC(10,2) NOT NULL CHECK (total_price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK ( status IN ('PENDING', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- таблица отзывов об объявлении (продавцах/покупателях)
CREATE TABLE IF NOT EXISTS ad_comment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES "order"(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Таблица платных продвижений объявлений
CREATE TABLE IF NOT EXISTS promotion (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL REFERENCES ad(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('TOP_7_DAYS', 'TOP_30_DAYS', 'HIGHLIGHT')),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL CHECK (end_date > start_date),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

create index if not exists idx_ad_status on ad(status);
CREATE INDEX IF NOT EXISTS idx_ad_seller_id ON ad(seller_id);
CREATE INDEX IF NOT EXISTS idx_ad_price ON ad(price);

CREATE INDEX IF NOT EXISTS idx_message_chat_id ON message(chat_id);
CREATE INDEX IF NOT EXISTS idx_chat_participant_user_id ON chat_participant(user_id);

CREATE INDEX IF NOT EXISTS idx_order_buyer_id ON "order"(buyer_id);
CREATE INDEX IF NOT EXISTS idx_order_seller_id ON "order"(seller_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON "order"(status);


-- Представление для получения объявлений с учетом рейтинга продавца и промо
CREATE OR REPLACE VIEW ad_search_view AS
SELECT
    a.id,
    a.title,
    a.description,
    a.price,
    a.quantity,
    a.status,
    a.seller_id,
    a.created_at,
    u.rating as seller_rating,
    CASE
        WHEN p.id IS NOT NULL AND p.active = TRUE AND p.end_date > NOW() THEN p.type
        ELSE NULL
        END as promotion_type,
    CASE
        WHEN p.id IS NOT NULL AND p.active = TRUE AND p.end_date > NOW() THEN 100 + u.rating * 10
        ELSE u.rating * 10
        END as search_score,
    (
        SELECT url
        FROM ad_image ai
        WHERE ai.ad_id = a.id AND ai.is_main = TRUE
        LIMIT 1
    ) as main_image_url
FROM ad a
         JOIN "user_rating" u ON a.seller_id = u.user_id
         LEFT JOIN promotion p ON a.id = p.ad_id
WHERE a.status = 'ACTIVE' AND a.quantity > 0;

-- Представление для статистики пользователя
CREATE OR REPLACE VIEW user_statistics AS
SELECT
    u.user_id,
    usr.name,
    u.rating,
    COUNT(DISTINCT a.id) as total_ads,
    COUNT(DISTINCT o.id) as total_sales,
    COALESCE(SUM(o.total_price), 0) as total_revenue
FROM user_rating u
         JOIN "user" usr ON u.user_id = usr.id
         LEFT JOIN ad a ON u.user_id = a.seller_id
         LEFT JOIN "order" o ON u.user_id = o.seller_id AND o.status = 'COMPLETED'
GROUP BY u.user_id, usr.name, u.rating;

COMMENT ON TABLE "user" IS 'Пользователи системы (продавцы и покупатели)';
COMMENT ON COLUMN "user_rating".rating IS 'Средний рейтинг пользователя от 0 до 5';
COMMENT ON TABLE ad IS 'Объявления о продаже';
COMMENT ON COLUMN ad.status IS 'Статус объявления: ACTIVE, SOLD, BOOKED';
COMMENT ON TABLE ad_comment IS 'Отзывы об объявлениях (влияют на рейтинг)';
COMMENT ON TABLE "order" IS 'История продаж/покупок';
COMMENT ON COLUMN "order".status IS 'PAID, SHIPPED, COMPLETED, CANCELLED';
COMMENT ON TABLE promotion IS 'Платные продвижения объявлений';
COMMENT ON COLUMN promotion.type IS 'TOP_7_DAYS, TOP_30_DAYS, HIGHLIGHT';

--changeset polentrampus:2 splitStatements:false
--comment: Функции и триггеры (требуют отключения разбиения по ;)

-- Функция для обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер для таблицы user
CREATE TRIGGER update_user_updated_at
    BEFORE UPDATE ON "user"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Триггер для таблицы ad
CREATE TRIGGER update_ad_updated_at
    BEFORE UPDATE ON ad
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Триггер для таблицы order
CREATE TRIGGER update_order_updated_at
    BEFORE UPDATE ON "order"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE FUNCTION create_user_rating()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO user_rating (user_id, rating) VALUES (NEW.id, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_user_rating
    AFTER INSERT ON "user"
    FOR EACH ROW EXECUTE FUNCTION create_user_rating();

CREATE OR REPLACE FUNCTION update_user_rating()
    RETURNS TRIGGER AS $$
DECLARE
    seller_id BIGINT;
BEGIN
    SELECT o.seller_id INTO seller_id
    FROM "order" o
    WHERE o.id = COALESCE(NEW.order_id, OLD.order_id);

    INSERT INTO user_rating (user_id, rating)
    VALUES (seller_id, (
        SELECT COALESCE(AVG(rating)::NUMERIC(3,2), 0)
        FROM ad_comment ac
                 JOIN "order" o ON ac.order_id = o.id
        WHERE o.seller_id = seller_id
    ))
    ON CONFLICT (user_id) DO UPDATE SET rating = EXCLUDED.rating;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Триггер на обновление рейтинга при добавлении/изменении/удалении отзыва
CREATE TRIGGER update_rating_after_review_change
    AFTER INSERT OR UPDATE OR DELETE ON ad_comment
    FOR EACH ROW
    EXECUTE FUNCTION update_user_rating();
