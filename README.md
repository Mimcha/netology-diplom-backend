# Cloud Service

## Описание
Сервис для загрузки и управления файлами с авторизацией через токен.

## Требования
- Java 11+
- Docker
- Maven

## Запуск
1. Собрать проект: `mvn clean package`
2. Запустить с помощью Docker: `docker-compose up`

## Endpoints
- **POST /login** - Авторизация. Принимает JSON с `login` и `password`. Возвращает JSON с `auth-token`.
- **POST /logout** - Выход. Удаляет токен.
- **POST /file** - Загрузка файла. Используйте `filename` в query и файл в multipart/form-data.
- **DELETE /file** - Удаление файла. Укажите `filename` в query.
- **GET /file** - Скачивание файла. Укажите `filename` в query.
- **PUT /file** - Переименование файла. Укажите `filename` в query и новое имя в теле `{ "name": "newname" }`.
- **GET /list** - Список файлов. Возвращает массив объектов `{ "filename": "...", "size": ... }`.

## Авторизация
Используется header `auth-token`. Получите его через `/login` и отправляйте во все запросы, кроме `/login`.

## Примеры использования

### Авторизация
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"login": "user", "password": "pass"}'