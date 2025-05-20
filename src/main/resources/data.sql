-- Создание пользователя
INSERT INTO users (id, login, password_hash)
VALUES (1, 'test_user', '$2a$10$GRLdNijw1Ag4rbsddzQ8qewxHNZ3qzoKzOxKbPmiSIsgu7tJVSeqW');

-- Создание токена (если есть)
INSERT INTO token (token, user_id, expiration)
VALUES ('test_token', 1, NOW() + INTERVAL '1 hour');

-- Создание тестовых файлов (если есть таблица file_metadata)
INSERT INTO file_metadata (id, original_name, stored_name, size, user_id)
VALUES (1, 'test.txt', 'abc123_test.txt', 1024, 1);