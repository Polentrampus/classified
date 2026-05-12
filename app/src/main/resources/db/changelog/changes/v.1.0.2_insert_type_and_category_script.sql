--liquibase formatted sql
--changeset polentrampus:4

-- ============================================================
-- 1. ГОРОДА (Россия, крупнейшие + малые для демонстрации)
-- ============================================================
INSERT INTO city (name) VALUES
                            ('Москва'),
                            ('Санкт-Петербург'),
                            ('Новосибирск'),
                            ('Екатеринбург'),
                            ('Казань'),
                            ('Нижний Новгород'),
                            ('Челябинск'),
                            ('Красноярск'),
                            ('Самара'),
                            ('Уфа'),
                            ('Ростов-на-Дону'),
                            ('Омск'),
                            ('Краснодар'),
                            ('Воронеж'),
                            ('Пермь'),
                            ('Волгоград'),
                            ('Калининград'),
                            ('Владивосток'),
                            ('Сочи'),
                            ('Тюмень')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- 2. КАТЕГОРИИ ТОВАРОВ (ad_category)
-- ============================================================
INSERT INTO ad_category (name) VALUES
                                   ('Электроника'),
                                   ('Одежда и обувь'),
                                   ('Детские товары'),
                                   ('Недвижимость'),
                                   ('Транспорт'),
                                   ('Спорт и отдых'),
                                   ('Животные'),
                                   ('Мебель и интерьер'),
                                   ('Книги и канцелярия'),
                                   ('Услуги'),
                                   ('Продукты питания'),
                                   ('Садоводство и растения'),
                                   ('Музыкальные инструменты'),
                                   ('Красота и здоровье'),
                                   ('Коллекционирование и хобби'),
                                   ('Стройматериалы и ремонт')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 3. ТИПЫ ПРОДУКТОВ (product_type)
--    Каждый тип логически принадлежит категории
-- ============================================================

-- Электроника (category_id = 1)
INSERT INTO product_type (name) VALUES
                                    ('Смартфоны'),
                                    ('Ноутбуки'),
                                    ('Планшеты'),
                                    ('Телевизоры'),
                                    ('Наушники и аудиотехника'),
                                    ('Фотоаппараты'),
                                    ('Игровые приставки'),
                                    ('Комплектующие для ПК'),
                                    ('Умные часы и гаджеты'),
                                    ('Электротранспорт');

-- Одежда и обувь (category_id = 2)
INSERT INTO product_type (name) VALUES
                                    ('Женская одежда'),
                                    ('Мужская одежда'),
                                    ('Детская одежда'),
                                    ('Обувь женская'),
                                    ('Обувь мужская'),
                                    ('Аксессуары');

-- Детские товары (category_id = 3)
INSERT INTO product_type (name) VALUES
                                    ('Коляски'),
                                    ('Детская мебель'),
                                    ('Игрушки'),
                                    ('Товары для кормления');

-- Недвижимость (category_id = 4)
INSERT INTO product_type (name) VALUES
                                    ('Квартиры'),
                                    ('Дома'),
                                    ('Земельные участки'),
                                    ('Коммерческая недвижимость');

-- Транспорт (category_id = 5)
INSERT INTO product_type (name) VALUES
                                    ('Автомобили'),
                                    ('Мотоциклы'),
                                    ('Грузовики и спецтехника'),
                                    ('Водный транспорт'),
                                    ('Запчасти и автотовары');

-- Спорт и отдых (category_id = 6)
INSERT INTO product_type (name) VALUES
                                    ('Спортивный инвентарь'),
                                    ('Велосипеды'),
                                    ('Туристическое снаряжение'),
                                    ('Тренажёры и фитнес');

-- Животные (category_id = 7)
INSERT INTO product_type (name) VALUES
                                    ('Собаки'),
                                    ('Кошки'),
                                    ('Аквариумистика'),
                                    ('Корма и товары для животных');

-- Мебель и интерьер (category_id = 8)
INSERT INTO product_type (name) VALUES
                                    ('Мягкая мебель'),
                                    ('Кухонная мебель'),
                                    ('Освещение'),
                                    ('Декор и аксессуары');

-- Книги и канцелярия (category_id = 9)
INSERT INTO product_type (name) VALUES
                                    ('Художественная литература'),
                                    ('Учебная литература'),
                                    ('Канцтовары');

-- Услуги (category_id = 10)
INSERT INTO product_type (name) VALUES
                                    ('Ремонт и строительство'),
                                    ('Обучение и курсы'),
                                    ('Красота и уход'),
                                    ('Перевозки');

-- Продукты питания (category_id = 11)
INSERT INTO product_type (name) VALUES
                                    ('Молочная продукция'),
                                    ('Овощи и фрукты'),
                                    ('Мясо и птица'),
                                    ('Готовая еда');

-- Садоводство и растения (category_id = 12)
INSERT INTO product_type (name) VALUES
                                    ('Саженцы и рассада'),
                                    ('Семена'),
                                    ('Садовый инвентарь');

-- Музыкальные инструменты (category_id = 13)
INSERT INTO product_type (name) VALUES
                                    ('Гитары'),
                                    ('Клавишные инструменты'),
                                    ('Ударные установки');

-- Красота и здоровье (category_id = 14)
INSERT INTO product_type (name) VALUES
                                    ('Косметика'),
                                    ('Парфюмерия'),
                                    ('Медицинские товары');

-- Коллекционирование и хобби (category_id = 15)
INSERT INTO product_type (name) VALUES
                                    ('Монеты и банкноты'),
                                    ('Марки'),
                                    ('Моделизм'),
                                    ('Настольные игры');

-- Стройматериалы и ремонт (category_id = 16)
INSERT INTO product_type (name) VALUES
                                    ('Стройматериалы'),
                                    ('Инструменты'),
                                    ('Электрика'),
                                    ('Сантехника');

-- ============================================================
-- 4. СВЯЗКА КАТЕГОРИЙ И ТИПОВ (ad_type)
--    Каждый product_type связывается с соответствующей категорией
--    ПРИМЕЧАНИЕ: этот скрипт предполагает, что строки в
--    product_type вставлялись БЛОКАМИ по категориям строго в том
--    порядке, как они перечислены выше. Если порядок нарушен или
--    в таблице уже есть другие записи, нужно заменить подзапросы
--    на явные SELECT ... WHERE name = '...'.
-- ============================================================

-- Электроника (category_id = 1, product_type id = 1..10)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 1
FROM product_type pt
WHERE pt.name IN (
                  'Смартфоны', 'Ноутбуки', 'Планшеты', 'Телевизоры',
                  'Наушники и аудиотехника', 'Фотоаппараты', 'Игровые приставки',
                  'Комплектующие для ПК', 'Умные часы и гаджеты', 'Электротранспорт'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Одежда и обувь (category_id = 2, product_type id = 11..16)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 2
FROM product_type pt
WHERE pt.name IN (
                  'Женская одежда', 'Мужская одежда', 'Детская одежда',
                  'Обувь женская', 'Обувь мужская', 'Аксессуары'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Детские товары (category_id = 3, product_type id = 17..20)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 3
FROM product_type pt
WHERE pt.name IN (
                  'Коляски', 'Детская мебель', 'Игрушки', 'Товары для кормления'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Недвижимость (category_id = 4, product_type id = 21..24)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 4
FROM product_type pt
WHERE pt.name IN (
                  'Квартиры', 'Дома', 'Земельные участки', 'Коммерческая недвижимость'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Транспорт (category_id = 5, product_type id = 25..29)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 5
FROM product_type pt
WHERE pt.name IN (
                  'Автомобили', 'Мотоциклы', 'Грузовики и спецтехника',
                  'Водный транспорт', 'Запчасти и автотовары'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Спорт и отдых (category_id = 6, product_type id = 30..33)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 6
FROM product_type pt
WHERE pt.name IN (
                  'Спортивный инвентарь', 'Велосипеды',
                  'Туристическое снаряжение', 'Тренажёры и фитнес'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Животные (category_id = 7, product_type id = 34..37)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 7
FROM product_type pt
WHERE pt.name IN (
                  'Собаки', 'Кошки', 'Аквариумистика', 'Корма и товары для животных'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Мебель и интерьер (category_id = 8, product_type id = 38..41)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 8
FROM product_type pt
WHERE pt.name IN (
                  'Мягкая мебель', 'Кухонная мебель', 'Освещение', 'Декор и аксессуары'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Книги и канцелярия (category_id = 9, product_type id = 42..44)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 9
FROM product_type pt
WHERE pt.name IN (
                  'Художественная литература', 'Учебная литература', 'Канцтовары'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Услуги (category_id = 10, product_type id = 45..48)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 10
FROM product_type pt
WHERE pt.name IN (
                  'Ремонт и строительство', 'Обучение и курсы',
                  'Красота и уход', 'Перевозки'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Продукты питания (category_id = 11, product_type id = 49..52)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 11
FROM product_type pt
WHERE pt.name IN (
                  'Молочная продукция', 'Овощи и фрукты', 'Мясо и птица', 'Готовая еда'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Садоводство и растения (category_id = 12, product_type id = 53..55)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 12
FROM product_type pt
WHERE pt.name IN (
                  'Саженцы и рассада', 'Семена', 'Садовый инвентарь'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Музыкальные инструменты (category_id = 13, product_type id = 56..58)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 13
FROM product_type pt
WHERE pt.name IN (
                  'Гитары', 'Клавишные инструменты', 'Ударные установки'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Красота и здоровье (category_id = 14, product_type id = 59..61)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 14
FROM product_type pt
WHERE pt.name IN (
                  'Косметика', 'Парфюмерия', 'Медицинские товары'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Коллекционирование и хобби (category_id = 15, product_type id = 62..65)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 15
FROM product_type pt
WHERE pt.name IN (
                  'Монеты и банкноты', 'Марки', 'Моделизм', 'Настольные игры'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;

-- Стройматериалы и ремонт (category_id = 16, product_type id = 66..69)
INSERT INTO ad_type (type_id, category_id)
SELECT pt.id, 16
FROM product_type pt
WHERE pt.name IN (
                  'Стройматериалы', 'Инструменты', 'Электрика', 'Сантехника'
    )
ON CONFLICT (type_id, category_id) DO NOTHING;