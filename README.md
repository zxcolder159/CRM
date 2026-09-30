# CRM - Backend API

Упрощённая CRM для управления продавцами и их транзакциями.

Реализован REST API на Spring Boot. Помимо стандартного CRUD есть три аналитических эндпоинта: самый продуктивный продавец за выбранный период, список продавцов с суммой транзакций ниже порога, и в качестве дополнительной задачи - определение лучшего периода продаж для конкретного продавца.

---

## Тестирование

Проект содержит набор автоматизированных тестов, разделенных по слоям приложения. Это позволяет изолированно проверять бизнес-требования и гарантировать корректность работы API при внесении изменений в код.

- **Слой контроллеров** (`@WebMvcTest` + MockMvc) — проверяется корректность работы HTTP-контрактов: маппинг URL, обработка параметров запроса, правильность возвращаемых HTTP-статусов, а также механизмы валидации входящих данных и сериализация/десериализация DTO.
- **Слой сервисов** (Mockito, JUnit 5) — изолированные юнит-тесты основной бизнес-логики. Тесты запускаются быстро, без поднятия контекста Spring. Они покрывают как успешные сценарии (happy path), так и различные граничные случаи в алгоритмах аналитики и агрегации данных.
- **Слой репозиториев** (`@DataJpaTest`) — интеграционные тесты для проверки взаимодействия с базой данных. Используется in-memory БД H2, настроенная в режиме совместимости с PostgreSQL, что позволяет тестировать работоспособность сложных кастомных JPQL-запросов в условиях, приближенных к боевым.

### Запуск тестов и покрытие

```bash
./gradlew test
./gradlew jacocoTestReport # Отчёт по покрытию (JaCoCo)
```

---

## Стек

Java 21, Spring Boot 4.0.6, Spring Data JPA, PostgreSQL, Gradle.
Для тестов - H2 in-memory в режиме PostgreSQL-совместимости.
Документация API - SpringDoc OpenAPI (Swagger UI) + Javadoc на публичных методах.

---

## Структура проекта

```
src/main/java/.../crm/
├── controller/     REST-контроллеры + GlobalExceptionHandler
├── dto/            входящие и исходящие DTO (records)
├── entity/         JPA-сущности Seller и Transaction
├── exception/      ResourceNotFoundException
├── repository/     Spring Data репозитории с JPQL-запросами
├── service/        бизнес-логика
└── util/           PaymentType и PeriodType (enum)
```

---

## Запуск

### Через Docker Compose

Создайте `.env` в корне - образец в `.env.example`:

```env
DB_PASSWORD=your_password_here
```

```bash
docker compose up --build
```

Приложение на `http://localhost:8080`, PostgreSQL на `5432`.

### Локально (если PostgreSQL уже поднят)

```bash
./gradlew bootRun
```

Или собрать JAR отдельно:

```bash
./gradlew bootJar
java -jar build/libs/crm-0.0.1-SNAPSHOT.jar
```

---

## Модель данных

**Seller** - продавец: `id`, `name`, `contactInfo`, `registrationDate`.

Удаление реализовано через soft delete: при вызове DELETE устанавливается флаг `isDeleted = true`, сама запись из базы не удаляется. На уровне сущности висит аннотация `@SQLRestriction("is_deleted = false")`, которая автоматически добавляет условие в каждый SELECT - удалённые продавцы просто не появляются ни в списках, ни в аналитике. Транзакции удалённых продавцов при этом сохраняются в базе, историчность данных не нарушается.

**Transaction** - транзакция: `id`, `seller`  (FK), `amount`, `paymentType` (CASH / CARD / TRANSFER), `transactionDate`.

---

## API

Swagger UI: `http://localhost:8080/swagger-ui.html`

### Продавцы - `/api/sellers`

| Метод  | URL                             | Описание                                  |
|--------|---------------------------------|-------------------------------------------|
| GET    | `/api/sellers`                  | Список всех продавцов                     |
| POST   | `/api/sellers`                  | Создать продавца                          |
| GET    | `/api/sellers/{id}`             | Получить продавца по ID                   |
| PUT    | `/api/sellers/{id}`             | Обновить данные продавца                  |
| DELETE | `/api/sellers/{id}`             | Удалить продавца (soft delete)            |
| GET    | `/api/sellers/most-productive`  | Самый продуктивный продавец за период     |
| GET    | `/api/sellers/underperforming`  | Продавцы с суммой ниже порога за период   |
| GET    | `/api/sellers/{id}/best-period` | Лучший период продаж для продавца         |

### Транзакции - `/api/transactions`

| Метод  | URL                                   | Описание                            |
|--------|---------------------------------------|-------------------------------------|
| GET    | `/api/transactions`                   | Список всех транзакций              |
| POST   | `/api/transactions`                   | Создать транзакцию                  |
| GET    | `/api/transactions/{id}`              | Транзакция по ID                    |
| GET    | `/api/transactions/seller/{sellerId}` | Транзакции конкретного продавца     |

Все списочные эндпоинты поддерживают пагинацию: `?page=0&size=20&sort=id,desc`.

### Параметр `periodType`

Аналитические эндпоинты принимают `periodType`: `DAY`, `WEEK`, `MONTH`, `QUARTER`, `YEAR`.
Неделя считается с понедельника, квартал - с первого числа первого месяца квартала.

---

## Примеры запросов

**Создать продавца:**

```http
POST /api/sellers
Content-Type: application/json

{
  "name": "Иван Иванов",
  "contactInfo": "ivan@example.com"
}
```

Ответ `201`:
```json
{
  "id": 1,
  "name": "Иван Иванов",
  "contactInfo": "ivan@example.com",
  "registrationDate": "2026-05-19T12:00:00"
}
```

**Создать транзакцию:**

```http
POST /api/transactions
Content-Type: application/json

{
  "sellerId": 1,
  "amount": 15000.50,
  "paymentType": "CARD"
}
```

**Самый продуктивный продавец за май:**

```http
GET /api/sellers/most-productive?startDate=2026-05-01T00:00:00&periodType=MONTH
```

**Продавцы с суммой транзакций меньше 10 000 за май:**

```http
GET /api/sellers/underperforming?startDate=2026-05-01T00:00:00&periodType=MONTH&threshold=10000
```

**Лучший месяц продаж продавца с id=1:**

```http
GET /api/sellers/1/best-period?periodType=MONTH
```

```json
{
  "sellerId": 1,
  "periodStart": "2026-04-01",
  "periodEnd": "2026-04-30",
  "periodType": "MONTH"
}
```

---

## Обработка ошибок

Все ошибки возвращаются в одном формате:

```json
{
  "timestamp": "2026-05-19T12:00:00",
  "message": "Продавец с id 99 не найден",
  "status": 404
}
```

400 - невалидное тело запроса или параметры, 404 - ресурс не найден, 409 - нарушение целостности данных, 500 - непредвиденная ошибка.
