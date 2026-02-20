# Restaurant Search

Spring Boot API for searching restaurants (technical assessment). It returns the best-matched restaurants based on optional filters: name, customer rating, distance, price, and cuisine. Results are sorted by distance (closest first), then rating (highest first), then price (lowest first), and limited to 5 by default (configurable).

## Search API

**Endpoint:** `GET /api/restaurants/search`

**Query parameters** (all optional; when multiple are provided they are combined with AND):

| Parameter         | Description                            | Valid range / format |
|------------------|----------------------------------------|----------------------|
| `name`           | Restaurant name (partial or exact)     | Any non-empty string |
| `customerRating` | Minimum customer rating (stars)        | 1–5                  |
| `distance`       | Maximum distance in miles              | 1.0–10.0             |
| `price`          | Maximum price per person ($)           | 10–50                |
| `cuisine`        | Cuisine (partial or exact)            | Any non-empty string |

Invalid values (e.g. rating 0 or 6, distance 0 or 11) return `400 Bad Request` with a JSON body `{"error": "..."}`. With no parameters, the API returns up to 5 restaurants sorted by best match.

## Prerequisites

- **Java 21** (for running with Maven)
- **Maven 3.6+** (for building and running locally)
- **Docker** (optional, for running via container)

## Run the application

### Option 1: Maven (recommended for local development)

```bash
mvn spring-boot:run
```

The API will be available at **http://localhost:8080**.

### Option 2: Docker

Build the image:

```bash
docker build -t restaurant-search .
```

Run the container:

```bash
docker run -p 8080:8080 restaurant-search
```

The API will be available at **http://localhost:8080**.

## Run tests

**Locally:**

```bash
mvn test
```

**Using Docker:** The image build runs tests automatically. If any test fails, `docker build` fails:

```bash
docker build -t restaurant-search .
```

## API documentation (Swagger)

With the application running, open in your browser:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

You can try the endpoints directly from Swagger UI.

## Quick test (curl)

With the app running:

```bash
# No parameters: returns up to 5 restaurants, sorted by best match
curl "http://localhost:8080/api/restaurants/search"

# With parameters (e.g. minimum 4 stars, max 2 miles, max $20)
curl "http://localhost:8080/api/restaurants/search?customerRating=4&distance=2&price=20"
```

## Architecture

- **Controller** — Handles `GET /api/restaurants/search`, maps query params to `SearchCriteria`, validates, calls the service, returns JSON or 400 with error message.
- **Validator** — Checks rating (1–5), distance (1–10), price (10–50). Returns an error message when a value is out of range.
- **Service** — Loads restaurants via `RestaurantLoader`, filters by name (non-blank) and by criteria (matcher), sorts by best match, limits to configured max results.
- **Matcher** — Pure logic: AND of all non-null criteria (name/rating/distance/price/cuisine); name and cuisine use case-insensitive partial match.
- **Sorter** — Orders by distance (asc), then rating (desc), then price (asc), then name (deterministic tie-break).
- **Loader** — Reads `restaurants.csv` and `cuisines.csv` from the classpath, caches in memory after first load.

## Assumptions

- **Tie-break after distance, rating, price:** When two restaurants have the same distance, rating and price, the order is determined by **restaurant name (alphabetical)** instead of random. This keeps results stable and testable.
- **Name and cuisine matching:** Case-insensitive partial match (e.g. `"Mcd"` matches `"McDonald's"`, `"Chi"` matches `"Chinese"`). Empty or blank search string means “no filter” for that criterion.
- **Missing cuisines file:** If `cuisines.csv` is missing or unreadable, cuisine IDs are resolved as `"Unknown"` and restaurants are still included in results.
- **Every result has a name:** Restaurants with null or blank name are excluded so that every returned record contains at least the restaurant name.
- **Max results:** Default is 5; configurable via `restaurant.search.max-results` in `application.properties` (or environment variable `RESTAURANT_SEARCH_MAX_RESULTS`).
