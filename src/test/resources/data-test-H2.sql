-- ==================== ПОЛЬЗОВАТЕЛИ ====================
-- ==================== ПОЛЬЗОВАТЕЛИ ====================
-- ==================== ПОЛЬЗОВАТЕЛИ ====================
INSERT INTO users (id, username, email, city, password, is_online, role, created_at, last_login) VALUES
(1, 'ivanov', 'ivanov@test.com', 'Москва', '$2a$12$49OcWSQdgl5Sl7T/EEQ7Fut51D3uBIo9VE7UL7icMAJFKPhu5g/Oe', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'petrov', 'petrov@test.com', 'Санкт-Петербург', '$2a$12$sfCed.nsl6wo3AI4sFKMveFacKgIvvrklSv/pSbtvkV/J5bMeEeNa', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'sidorova', 'sidorova@test.com', 'Казань', '$2a$12$PaWUGR1Keq.y1YGv9s90AOppX2g/OvjHoNiZJocsEVzLqV7EE58va', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'kuznetsova', 'kuznetsova@test.com', 'Москва', '$2a$12$zfFMkQ9c/rRZeha0R0FPCuLGPDXAHFVw1/PAybbirRc1PNt84wRt.', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'smirnov', 'smirnov@test.com', 'Новосибирск', '$2a$12$X9QvcLJ0zPgpRnNRlF9xh.SniQ3Vz2mNefCCIQ76X56IGdAAiFYKW', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'popova', 'popova@test.com', 'Москва', '$2a$12$O34UgN7Xm3XQzZSiU.NwNOBFAkXzOWdLl3ScAukO4E3oFouKBw0wm', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'volkov', 'volkov@test.com', 'Екатеринбург', '$2a$12$FkBGiM1zDNm/D.d1wtnKfuFAhyo3M5zqyGs5IhSM4jdI4GWR5mCbK', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'novikov', 'novikov@test.com', 'Краснодар', '$2a$12$fNJuOIkxDEEXE04OnV.BO.wQ0QdO501Lv.5Uj/G4mnBjUFYewL4/S', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'fedorov', 'fedorov@test.com', 'Воронеж', '$2a$12$bn8tiwGlTR5qNiIuf5UyrevSir03sRkQJqFPHyTu827.muwjyRz8O', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'morozova', 'morozova@test.com', 'Москва', '$2a$12$y3sLxeBoK6UQk47p4awATuIlpdunzBK1X5AB4oYefP2gHcCYyStZq', false, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==================== ПРОФИЛИ ====================
INSERT INTO profile (id, user_id, bio, profile_picture_url, city, date_of_birth, created_at) VALUES
(1, 1, 'Люблю программирование и путешествия', '/avatars/ivanov.jpg', 'Москва', '1999-01-15', CURRENT_TIMESTAMP),
(2, 2, 'Фотограф и путешественник', '/avatars/petrov.jpg', 'Санкт-Петербург', '1998-03-20', CURRENT_TIMESTAMP),
(3, 3, 'Студентка, увлекаюсь искусством', '/avatars/sidorova.jpg', 'Казань', '2000-07-10', CURRENT_TIMESTAMP),
(4, 4, 'Дизайнер, люблю современное искусство', '/avatars/kuznetsova.jpg', 'Москва', '1999-08-25', CURRENT_TIMESTAMP),
(5, 5, 'Инженер, автолюбитель', '/avatars/smirnov.jpg', 'Новосибирск', '1985-12-30', CURRENT_TIMESTAMP),
(6, 6, 'Маркетолог, веду блог о путешествиях', '/avatars/popova.jpg', 'Москва', '1998-11-05', CURRENT_TIMESTAMP),
(7, 7, 'Предприниматель в IT сфере', '/avatars/volkov.jpg', 'Екатеринбург', '1997-04-18', CURRENT_TIMESTAMP),
(8, 8, 'Спортсмен, тренер по футболу', '/avatars/novikov.jpg', 'Краснодар', '1999-06-12', CURRENT_TIMESTAMP),
(9, 9, 'Врач, увлекаюсь научной фантастикой', '/avatars/fedorov.jpg', 'Воронеж', '1990-09-22', CURRENT_TIMESTAMP),
(10, 10, 'Студентка журфака, пишу статьи', '/avatars/morozova.jpg', 'Москва', '2001-02-28', CURRENT_TIMESTAMP);

-- ==================== ОТНОШЕНИЯ (relation_ship) ====================
INSERT INTO relation_ship (id, user_sender_id, user_receiver_id, status, time_updated, created_at) VALUES
-- Иванов дружит с Петровым и Сидоровой
(1, 1, 2, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 1, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 3, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, 1, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Петров дружит с Кузнецовой (сильная связь для Иванова)
(5, 2, 4, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 4, 2, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Сидорова дружит со Смирновым (средняя связь)
(7, 3, 5, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 5, 3, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Создаем сложную сеть друзей для тестирования рекомендаций
(9, 4, 6, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 6, 4, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 5, 7, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 7, 5, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 6, 8, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 8, 6, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 7, 9, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 9, 7, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 8, 10, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 10, 8, 'ACCEPTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==================== ПОСТЫ ====================
INSERT INTO posts (id, user_id, content, image_url, created_at) VALUES
-- Пост Петрова (друг Иванова)
(1, 2, 'Отличная погода для прогулки в парке!', '/posts/park.jpg', CURRENT_TIMESTAMP),

-- Пост Сидоровой (друг Иванова)
(2, 3, 'Посетила интересную выставку современного искусства', '/posts/exhibition.jpg', CURRENT_TIMESTAMP),

-- Пост Кузнецовой (кандидат)
(3, 4, 'Новый проект в разработке! Скоро покажу результат', '/posts/project.jpg', CURRENT_TIMESTAMP),

-- Пост Смирнова (кандидат)
(4, 5, 'Отличная поездка на природу с друзьями', '/posts/nature.jpg', CURRENT_TIMESTAMP),

-- Пост Поповой (кандидат)
(5, 6, 'Мои новые фотографии из путешествия по Европе', '/posts/europe.jpg', CURRENT_TIMESTAMP);

-- ==================== КОММЕНТАРИИ (comment) ====================
INSERT INTO comment (id, post_id, creator_id, content, image_url, created_at) VALUES
-- Комментарии к посту Петрова
(1, 1, 4, 'Согласен, отличная погода!', NULL, CURRENT_TIMESTAMP),
(2, 1, 5, 'Какой парк?', NULL, CURRENT_TIMESTAMP),

-- Комментарии к посту Сидоровой
(3, 2, 1, 'Какая выставка? Хотел бы тоже посетить', NULL, CURRENT_TIMESTAMP),
(4, 2, 6, 'Очень интересно!', NULL, CURRENT_TIMESTAMP);

-- ==================== ЛАЙКИ (likes) ====================
INSERT INTO likes (id, user_id, post_id, comment_id, created_at) VALUES
-- Лайки на посты
-- Иванов и Кузнецова лайкают пост Петрова (сильная связь)
(1, 1, 1, NULL, CURRENT_TIMESTAMP),
(2, 4, 1, NULL, CURRENT_TIMESTAMP),

-- Иванов и Смирнов лайкают пост Сидоровой (средняя связь)
(3, 1, 2, NULL, CURRENT_TIMESTAMP),
(4, 5, 2, NULL, CURRENT_TIMESTAMP),

-- Иванов и Попова лайкают пост Кузнецовой (слабая связь)
(5, 1, 3, NULL, CURRENT_TIMESTAMP),
(6, 6, 3, NULL, CURRENT_TIMESTAMP),

-- Лайки на комментарии
-- Иванов и Новиков лайкают один комментарий
(7, 1, NULL, 1, CURRENT_TIMESTAMP),
(8, 8, NULL, 1, CURRENT_TIMESTAMP),

-- Иванов и Волков лайкают другой комментарий
(9, 1, NULL, 2, CURRENT_TIMESTAMP),
(10, 7, NULL, 2, CURRENT_TIMESTAMP);

-- ==================== СООБЩЕНИЯ ====================
INSERT INTO messages (id, sender_id, receiver_id, content, image_url, status, time_update, created_at) VALUES
-- Иванов общался с Волковым (сильный фактор общения)
(1, 1, 7, 'Привет! Как дела?', NULL, 'SENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 7, 1, 'Привет! Все отлично, работаю над новым проектом', NULL, 'SENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 7, 'Здорово! Расскажешь поподробнее?', NULL, 'SENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Иванов общался с Федоровым (одно сообщение)
(4, 1, 9, 'Здравствуйте!', NULL, 'SENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
