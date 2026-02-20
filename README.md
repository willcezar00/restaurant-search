# Restaurant Search

Spring Boot API for searching restaurants (technical assessment).

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
curl "http://localhost:8080/api/restaurants/search"
```

Returns up to 5 restaurants (empty list if no data or no criteria). Add query params: `name`, `customerRating`, `distance`, `price`, `cuisine`.
