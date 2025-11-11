-- Сначала удалить данные из дочерних таблиц
DELETE FROM likes;
DELETE FROM comment;
DELETE FROM messages;
DELETE FROM posts;
DELETE FROM relation_ship;
DELETE FROM notifications;
DELETE FROM profile;

-- Затем удалить пользователей
DELETE FROM users;

-- ==================== ПОЛЬЗОВАТЕЛИ ====================
INSERT INTO users (id, username, email, city, password, is_online, role, created_at, last_login) VALUES
(1, 'ivanov', 'ivanov@test.com', 'Москва', '$2a$12$49OcWSQdgl5Sl7T/EEQ7Fut51D3uBIo9VE7UL7icMAJFKPhu5g/Oe', false, 'ROLE_USER', NOW(), NOW()),
(2, 'petrov', 'petrov@test.com', 'Санкт-Петербург', '$2a$12$sfCed.nsl6wo3AI4sFKMveFacKgIvvrklSv/pSbtvkV/J5bMeEeNa', false, 'ROLE_USER', NOW(), NOW()),
(3, 'sidorova', 'sidorova@test.com', 'Казань', '$2a$12$PaWUGR1Keq.y1YGv9s90AOppX2g/OvjHoNiZJocsEVzLqV7EE58va', false, 'ROLE_USER', NOW(), NOW()),
(4, 'kuznetsova', 'kuznetsova@test.com', 'Москва', '$2a$12$zfFMkQ9c/rRZeha0R0FPCuLGPDXAHFVw1/PAybbirRc1PNt84wRt.', false, 'ROLE_USER', NOW(), NOW()),
(5, 'smirnov', 'smirnov@test.com', 'Новосибирск', '$2a$12$X9QvcLJ0zPgpRnNRlF9xh.SniQ3Vz2mNefCCIQ76X56IGdAAiFYKW', false, 'ROLE_USER', NOW(), NOW()),
(6, 'popova', 'popova@test.com', 'Москва', '$2a$12$O34UgN7Xm3XQzZSiU.NwNOBFAkXzOWdLl3ScAukO4E3oFouKBw0wm', false, 'ROLE_USER', NOW(), NOW()),
(7, 'volkov', 'volkov@test.com', 'Екатеринбург', '$2a$12$FkBGiM1zDNm/D.d1wtnKfuFAhyo3M5zqyGs5IhSM4jdI4GWR5mCbK', false, 'ROLE_USER', NOW(), NOW()),
(8, 'novikov', 'novikov@test.com', 'Краснодар', '$2a$12$fNJuOIkxDEEXE04OnV.BO.wQ0QdO501Lv.5Uj/G4mnBjUFYewL4/S', false, 'ROLE_USER', NOW(), NOW()),
(9, 'fedorov', 'fedorov@test.com', 'Воронеж', '$2a$12$bn8tiwGlTR5qNiIuf5UyrevSir03sRkQJqFPHyTu827.muwjyRz8O', false, 'ROLE_USER', NOW(), NOW()),
(10, 'morozova', 'morozova@test.com', 'Москва', '$2a$12$y3sLxeBoK6UQk47p4awATuIlpdunzBK1X5AB4oYefP2gHcCYyStZq', false, 'ROLE_USER', NOW(), NOW());

-- Сброс последовательности для users
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- ==================== ПРОФИЛИ ====================
INSERT INTO profile (id, user_id, bio, profile_picture_url, city, date_of_birth, created_at) VALUES
(1, 1, 'Люблю программирование и путешествия', '/avatars/ivanov.jpg', 'Москва', '1999-01-15', NOW()),
(2, 2, 'Фотограф и путешественник', '/avatars/petrov.jpg', 'Санкт-Петербург', '1998-03-20', NOW()),
(3, 3, 'Студентка, увлекаюсь искусством', '/avatars/sidorova.jpg', 'Казань', '2000-07-10', NOW()),
(4, 4, 'Дизайнер, люблю современное искусство', '/avatars/kuznetsova.jpg', 'Москва', '1999-08-25', NOW()),
(5, 5, 'Инженер, автолюбитель', '/avatars/smirnov.jpg', 'Новосибирск', '1985-12-30', NOW()),
(6, 6, 'Маркетолог, веду блог о путешествиях', '/avatars/popova.jpg', 'Москва', '1998-11-05', NOW()),
(7, 7, 'Предприниматель в IT сфере', '/avatars/volkov.jpg', 'Екатеринбург', '1997-04-18', NOW()),
(8, 8, 'Спортсмен, тренер по футболу', '/avatars/novikov.jpg', 'Краснодар', '1999-06-12', NOW()),
(9, 9, 'Врач, увлекаюсь научной фантастикой', '/avatars/fedorov.jpg', 'Воронеж', '1990-09-22', NOW()),
(10, 10, 'Студентка журфака, пишу статьи', '/avatars/morozova.jpg', 'Москва', '2001-02-28', NOW());

-- Сброс последовательности для profile
SELECT setval('profile_id_seq', (SELECT MAX(id) FROM profile));

-- ==================== ОТНОШЕНИЯ (relation_ship) ====================
INSERT INTO relation_ship (id, user_sender_id, user_receiver_id, status, time_updated, created_at) VALUES
(1, 1, 2, 'ACCEPTED', NOW(), NOW()),
(2, 2, 1, 'ACCEPTED', NOW(), NOW()),
(3, 1, 3, 'ACCEPTED', NOW(), NOW()),
(4, 3, 1, 'ACCEPTED', NOW(), NOW()),
(5, 2, 4, 'ACCEPTED', NOW(), NOW()),
(6, 4, 2, 'ACCEPTED', NOW(), NOW()),
(7, 3, 5, 'ACCEPTED', NOW(), NOW()),
(8, 5, 3, 'ACCEPTED', NOW(), NOW()),
(9, 4, 6, 'ACCEPTED', NOW(), NOW()),
(10, 6, 4, 'ACCEPTED', NOW(), NOW()),
(11, 5, 7, 'ACCEPTED', NOW(), NOW()),
(12, 7, 5, 'ACCEPTED', NOW(), NOW()),
(13, 6, 8, 'ACCEPTED', NOW(), NOW()),
(14, 8, 6, 'ACCEPTED', NOW(), NOW()),
(15, 7, 9, 'ACCEPTED', NOW(), NOW()),
(16, 9, 7, 'ACCEPTED', NOW(), NOW()),
(17, 8, 10, 'ACCEPTED', NOW(), NOW()),
(18, 10, 8, 'ACCEPTED', NOW(), NOW());

-- Сброс последовательности для relation_ship
SELECT setval('relation_ship_id_seq', (SELECT MAX(id) FROM relation_ship));

-- ==================== ПОСТЫ ====================
INSERT INTO posts (id, user_id, content, image_url, created_at) VALUES
(1, 2, 'Отличная погода для прогулки в парке!', '/posts/park.jpg', NOW()),
(2, 3, 'Посетила интересную выставку современного искусства', '/posts/exhibition.jpg', NOW()),
(3, 4, 'Новый проект в разработке! Скоро покажу результат', '/posts/project.jpg', NOW()),
(4, 5, 'Отличная поездка на природу с друзьями', '/posts/nature.jpg', NOW()),
(5, 6, 'Мои новые фотографии из путешествия по Европе', '/posts/europe.jpg', NOW());

-- Сброс последовательности для posts
SELECT setval('posts_id_seq', (SELECT MAX(id) FROM posts));

-- ==================== КОММЕНТАРИИ (comment) ====================
INSERT INTO comment (id, post_id, creator_id, content, image_url, created_at) VALUES
(1, 1, 4, 'Согласен, отличная погода!', NULL, NOW()),
(2, 1, 5, 'Какой парк?', NULL, NOW()),
(3, 2, 1, 'Какая выставка? Хотел бы тоже посетить', NULL, NOW()),
(4, 2, 6, 'Очень интересно!', NULL, NOW());

-- Сброс последовательности для comment
SELECT setval('comment_id_seq', (SELECT MAX(id) FROM comment));

-- ==================== ЛАЙКИ (likes) ====================
INSERT INTO likes (id, user_id, post_id, comment_id, created_at) VALUES
(1, 1, 1, NULL, NOW()),
(2, 4, 1, NULL, NOW()),
(3, 1, 2, NULL, NOW()),
(4, 5, 2, NULL, NOW()),
(5, 1, 3, NULL, NOW()),
(6, 6, 3, NULL, NOW()),
(7, 1, NULL, 1, NOW()),
(8, 8, NULL, 1, NOW()),
(9, 1, NULL, 2, NOW()),
(10, 7, NULL, 2, NOW());

-- Сброс последовательности для likes
SELECT setval('likes_id_seq', (SELECT MAX(id) FROM likes));

-- ==================== СООБЩЕНИЯ ====================
INSERT INTO messages (id, sender_id, receiver_id, content, image_url, status, time_update, created_at) VALUES
(1, 1, 7, 'Привет! Как дела?', NULL, 'SENT', NOW(), NOW()),
(2, 7, 1, 'Привет! Все отлично, работаю над новым проектом', NULL, 'SENT', NOW(), NOW()),
(3, 1, 7, 'Здорово! Расскажешь поподробнее?', NULL, 'SENT', NOW(), NOW()),
(4, 1, 9, 'Здравствуйте!', NULL, 'SENT', NOW(), NOW());

-- Сброс последовательности для messages
SELECT setval('messages_id_seq', (SELECT MAX(id) FROM messages));