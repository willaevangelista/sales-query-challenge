<div align='center' id='top'>

# Sales Query Challenge - Spring Boot REST API

  ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
  ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
  ![Hibernate](https://img.shields.io/badge/hibernate-%2359666C.svg?style=for-the-badge&logo=hibernate&logoColor=white)
  ![H2](https://img.shields.io/badge/h2database-%23336791.svg?style=for-the-badge&logo=h2&logoColor=white)
  ![Maven](https://img.shields.io/badge/maven-%23C71A36.svg?style=for-the-badge&logo=apachemaven&logoColor=white)

</div>

A Spring Boot application built to practice **custom JPQL queries, pagination, and query parameter handling** with Spring Data JPA. The project implements two read-only reporting endpoints for a sales domain: a paginated sales report with optional filters, and a sales summary grouped and aggregated by seller.
<br><br>
The goal of this project is to expose a REST web service that reports on `Sale` records, each of which belongs to a `Seller`. The API must support an optional date range and seller name filter for a paginated sales report, as well as an optional date range filter for a sales summary aggregated by seller.

---

## Table of Contents
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Project Structure](#projectStructure)
- [Domain Model](#domainModel)
- [Endpoints](#endpoints)
- [Query Rules](#queryRules)
- [Seeding Data](#seedingData)
- [Running Locally](#runningLocally)
- [Testing with Postman](#testingWithPostman)
- [License](#license)

---

<div id='technologies'/>

## Technologies

| Badge | Technology | Purpose |
|---|---|---|
| ![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) | Java | Programming language |
| ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) | Spring Boot | Application framework |
| ![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white) | Hibernate / JPA | Object-Relational Mapping (ORM) |
| ![H2](https://img.shields.io/badge/H2_Database-336791?style=for-the-badge&logo=h2&logoColor=white) | H2 Database | In-memory relational database (test/dev environment) |
| ![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white) | Maven | Build and dependency management |

---

<div id='architecture'/>

## Architecture

This project follows a classic **layered architecture**, separating responsibilities across controllers, services, DTOs, and repositories:

- **controllers**: expose the REST endpoints, receive raw request parameters as `String`, and delegate to the service layer.
- **services**: parse and validate incoming parameters (converting `String` to `LocalDate`), apply default values for optional filters, and orchestrate repository calls.
- **entities**: contain the JPA entities (`Sale`, `Seller`), mapped through standard Hibernate annotations, with a `@ManyToOne`/`@OneToMany` relationship between them.
- **dto**: Data Transfer Objects used as JPQL constructor-expression projections, decoupling the REST API response from the persistence model.
- **repositories**: Spring Data JPA repositories with custom JPQL queries (`@Query`) for filtering, pagination, and aggregation.

---

<div id='projectStructure'/>

## Project Structure

```
sales-query-challenge/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/devsuperior/dsmeta/
│   │   │       ├── controllers/
│   │   │       │   └── SaleController.java
│   │   │       ├── dto/
│   │   │       │   ├── SaleMinDTO.java
│   │   │       │   ├── SaleReportDTO.java
│   │   │       │   └── SaleSummaryDTO.java
│   │   │       ├── entities/
│   │   │       │   ├── Sale.java
│   │   │       │   └── Seller.java
│   │   │       ├── repositories/
│   │   │       │   └── SaleRepository.java
│   │   │       ├── services/
│   │   │       │   └── SaleService.java
│   │   │       └── DsmetaApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── banner.txt
│   │       └── import.sql
│   └── test/
│       └── java/
│           └── com/devsuperior/dsmeta/
│               └── ... (unit and integration tests)
├── .gitignore
├── LICENSE
└── README.md
```

---

<div id='domainModel'/>

## Domain Model

The `Sale` entity represents a sale made to a customer and contains the following attributes:

| Attribute | Type | Description |
|---|---|---|
| `id` | `Long` | Unique identifier |
| `visited` | `Integer` | Number of customer visits |
| `deals` | `Integer` | Number of deals closed |
| `amount` | `Double` | Amount sold |
| `date` | `LocalDate` | Date the sale took place |
| `seller` | `Seller` | The seller responsible for the sale (`@ManyToOne`) |

The `Seller` entity represents a salesperson and contains the following attributes:

| Attribute | Type | Description |
|---|---|---|
| `id` | `Long` | Unique identifier |
| `name` | `String` | Seller's full name |
| `email` | `String` | Seller's email |
| `phone` | `String` | Seller's phone number |
| `sales` | `List<Sale>` | Sales made by this seller (`@OneToMany`, mapped by `seller`) |

Each `Sale` belongs to exactly one `Seller`, and a `Seller` can have many `Sale` records.

---

<div id='endpoints'/>

## Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/sales/{id}` | Find a sale by id |
| `GET` | `/sales/report` | Paginated sales report, optionally filtered by date range and seller name |
| `GET` | `/sales/summary` | Sales summary grouped by seller, optionally filtered by date range |

---

<div id='queryRules'/>

## Query Rules

Both `/sales/report` and `/sales/summary` accept the same optional filters:

- **`minDate`** *(optional, `String`, format `yyyy-MM-dd`)* — start of the date range. If not informed, defaults to **one year before `maxDate`**.
- **`maxDate`** *(optional, `String`, format `yyyy-MM-dd`)* — end of the date range. If not informed, defaults to **the current system date**.
- **`name`** *(optional, `String`, `/sales/report` only)* — a partial, case-insensitive match against the seller's name. If not informed, defaults to an **empty string**, matching every seller.

The controller receives all filters as raw `String` parameters; the service layer is responsible for parsing them into `LocalDate` and applying the default values described above, so that date resolution logic lives in a single place.

### `/sales/report`

Returns a paginated list of sales (`id`, `date`, `amount`, `sellerName`) whose `date` falls within `[minDate, maxDate]` and whose seller name contains `name`.

```
GET /sales/report?minDate=2022-05-01&maxDate=2022-05-31&name=odinson
```

```json
{
  "content": [
    {
      "id": 9,
      "date": "2022-05-22",
      "amount": 19476.0,
      "sellerName": "Loki Odinson"
    },
    {
      "id": 10,
      "date": "2022-05-18",
      "amount": 20530.0,
      "sellerName": "Thor Odinson"
    }
  ]
}
```

### `/sales/summary`

Returns a list of sellers with the sum of their sales (`SUM(amount)`, grouped by seller) whose `date` falls within `[minDate, maxDate]`.

```
GET /sales/summary?minDate=2022-01-01&maxDate=2022-06-30
```

```json
[
  {
    "sellerName": "Anakin",
    "total": 110571.0
  },
  {
    "sellerName": "Loki Odinson",
    "total": 150597.0
  }
]
```

---

<div id='seedingData'/>

## Seeding Data

On startup, the application populates the H2 in-memory database with sellers and sales via `import.sql`, allowing both endpoints to be tested immediately without any manual data entry.

---

<div id='runningLocally'/>

## Running Locally

```bash
./mvnw spring-boot:run
```

Once the application starts, the H2 console will be available at:

```
http://localhost:8080/h2-console
```

Use the JDBC URL configured in `application.properties` to connect and explore the seeded data.

The REST API will be available at:

```
http://localhost:8080/sales
```

---

<div id='testingWithPostman'/>

## Testing with Postman

A ready-to-use Postman collection is provided by the course to validate the implementation, covering:

- Sales summary by seller for a given period, and for the last 12 months (default period).
- Sales report for a given period/seller, and for the last 12 months (default period).

---

<div id='license'/>

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.

<div align='right'>

  [Back to top of page ⬆️](#top)

</div>
