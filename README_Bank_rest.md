# Система управления банковскими картами

Описать данный файл по ТЗ

## Запуск для разработки

1. Создать файл `.env` в корне проекта:

DB_NAME=bank
DB_USER=bank_user
DB_PASSWORD=bank_password
DB_HOST=localhost
DB_PORT=5432

2. Поднять базу данных:

docker-compose up -d db

3. Запустить Spring Boot приложение (из IDE или командой):

./mvnw spring-boot:run

4. Приложение будет доступно на http://localhost:8080, Swagger UI — на http://localhost:8080/swagger-ui.html