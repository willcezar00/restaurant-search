# Restaurant Search

REST API that finds the best-matched restaurants based on optional filters: name, customer rating, distance, price, and cuisine. Results are sorted by best match (closest, highest-rated, cheapest) and limited to 5 results by default.

## Tech stack

- **Java 21** with **Spring Boot 3.2**
- **Maven** for build and dependency management
- **JUnit 5 + Mockito** for testing (unit and integration)
- **springdoc-openapi** for Swagger / OpenAPI documentation
- **Docker** (multi-stage build) for containerized deployment

## Prerequisites

- **Java 21**
- **Maven 3.6+**
- **Docker** (optional)

## Run the application

### Option 1: Maven

```bash
mvn spring-boot:run
```

### Option 2: Docker

```bash
docker build -t restaurant-search .
docker run -p 8080:8080 restaurant-search
```

The API will be available at **http://localhost:8080**.

## Run tests

```bash
mvn test
```

Docker also runs tests during the build stage -- if any test fails, `docker build` fails.

## API

### `GET /api/restaurants/search`

All parameters are optional. When multiple are provided they are combined with AND.

| Parameter        | Type    | Description                        | Valid range  |
|------------------|---------|------------------------------------|--------------|
| `name`           | String  | Restaurant name (partial, case-insensitive) | non-empty    |
| `customerRating` | Integer | Minimum customer rating (stars)    | 1 – 5        |
| `distance`       | Double  | Maximum distance in miles          | 1.0 – 10.0   |
| `price`          | Integer | Maximum price per person ($)       | 10 – 50      |
| `cuisine`        | String  | Cuisine type (partial, case-insensitive) | non-empty    |

### Example request

```bash
curl "http://localhost:8080/api/restaurants/search?customerRating=4&distance=2&price=20"
```

### Success response (200)

```json
[
  {
    "name": "Deliciousgenics",
    "customerRating": 4,
    "distance": 1.0,
    "price": 10,
    "cuisine": "Chinese"
  }
]
```

### Validation error response (400)

When one or more parameters are out of range, all validation errors are returned at once:

```json
{
  "errors": [
    "customerRating must be between 1 and 5",
    "distance must be between 1 and 10 miles"
  ]
}
```

### Swagger UI

With the application running: http://localhost:8080/swagger-ui.html

## Project structure

```
src/main/java/org/example/
├── Main.java                              Application entry point
├── controller/
│   └── RestaurantSearchController.java    REST endpoint, input mapping, validation
├── domain/
│   ├── Restaurant.java                    Restaurant record (name, rating, distance, price, cuisine)
│   └── SearchCriteria.java                Search parameters value object
├── loader/
│   ├── RestaurantLoader.java              Interface for data access
│   └── CsvRestaurantLoader.java           CSV parser with in-memory caching
├── service/
│   ├── RestaurantSearchService.java       Orchestrates filter → sort → limit pipeline
│   ├── RestaurantMatcher.java             Filtering logic (AND of all criteria)
│   └── RestaurantSorter.java              Sorting comparator (distance, rating, price, name)
└── validation/
    ├── SearchCriteriaValidator.java        Parameter range validation with i18n
    └── ValidationError.java                Validation error record (message key + args)

src/test/java/org/example/
├── integration/
│   └── RestaurantSearchIntegrationTest.java   Full Spring context + MockMvc
└── unit/
    ├── controller/RestaurantSearchControllerTest.java
    ├── loader/CsvRestaurantLoaderTest.java
    ├── service/
    │   ├── RestaurantMatcherTest.java
    │   ├── RestaurantSearchServiceTest.java
    │   └── RestaurantSorterTest.java
    └── validation/SearchCriteriaValidatorTest.java
```

## Architecture

The application follows a **layered architecture** with clear separation of concerns:

- **Controller** — Maps HTTP query parameters to `SearchCriteria`, delegates validation and search, returns JSON or 400 with error details.
- **Validator** — Checks parameter ranges (rating 1–5, distance 1–10, price 10–50). Returns all validation errors at once rather than failing on the first.
- **Service** — Single stream pipeline: loads data → filters by non-blank name → filters by criteria → sorts by best match → limits results.
- **Matcher** — Pure filtering logic: AND of all non-null criteria. Name and cuisine use case-insensitive partial match. Predicates are defined as a list for easy extension.
- **Sorter** — Composite comparator: distance (asc) → rating (desc) → price (asc) → name (asc, deterministic tie-break).
- **Loader** — Reads CSV resources via Spring's `Resource` abstraction (supports classpath, filesystem, URL). Parses and caches in memory after first load.

## Design decisions

- **REST API with Spring Boot**: Chose to expose the search as a REST endpoint rather than a CLI, making it easier to demonstrate, test interactively (Swagger), and integrate with front-end clients.
- **Single stream pipeline**: The search service chains `filter → sort → limit` in one stream, avoiding intermediate collections and keeping the logic declarative and readable.
- **Predicate list pattern**: Both `RestaurantMatcher` and `SearchCriteriaValidator` define their rules as a list of functions, making it straightforward to add new criteria or validation rules without modifying existing logic.
- **Spring Resource abstraction**: The CSV loader accepts any Spring `Resource` (classpath, file, URL), making data sources configurable per environment without code changes.
- **In-memory caching**: CSV data is loaded once and cached, since the dataset is small and static. The `synchronized` keyword ensures thread safety.
- **Validation returns all errors**: Instead of failing on the first invalid parameter, the validator collects and returns every error so the client can fix all issues in one round trip.
- **i18n-ready validation messages**: Error messages are resolved through Spring's `MessageSource`, allowing localization without changing validation logic.
- **Java records for domain objects**: `Restaurant` and `SearchCriteria` are records, reducing boilerplate and signaling that these are immutable data carriers.

## Assumptions

- **Tie-break order**: When restaurants share the same distance, rating, and price, they are ordered by **name (alphabetical)** instead of randomly. This keeps results stable and testable.
- **Partial matching**: Name and cuisine use case-insensitive substring match (e.g. `"Mcd"` matches `"McDonald's"`, `"Chi"` matches `"Chinese"`). An empty or blank value means "no filter" for that field.
- **Missing cuisines file**: If `cuisines.csv` is missing or unreadable, cuisine IDs are resolved as `"Unknown"` and restaurants are still returned.
- **Every result has a name**: Restaurants with null or blank names are filtered out, ensuring every returned record contains at least the restaurant name (as stated in the requirements).
- **Max results**: Default is 5; configurable via `restaurant.search.max-results` in `application.properties`.

## Configuration

Key properties in `application.properties` (all have sensible defaults):

| Property                                  | Default             | Description                 |
|-------------------------------------------|---------------------|-----------------------------|
| `restaurant.search.max-results`           | `5`                 | Maximum results returned    |
| `restaurant.data.restaurants-file`        | `classpath:restaurants.csv` | Restaurants data source |
| `restaurant.data.cuisines-file`           | `classpath:cuisines.csv`    | Cuisines data source    |
| `restaurant.search.criteria.rating-min`   | `1`                 | Min allowed rating filter   |
| `restaurant.search.criteria.rating-max`   | `5`                 | Max allowed rating filter   |
| `restaurant.search.criteria.distance-min` | `1`                 | Min allowed distance filter |
| `restaurant.search.criteria.distance-max` | `10`                | Max allowed distance filter |
| `restaurant.search.criteria.price-min`    | `10`                | Min allowed price filter    |
| `restaurant.search.criteria.price-max`    | `50`                | Max allowed price filter    |
