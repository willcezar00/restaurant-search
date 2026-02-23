package org.example.unit.loader;

import org.example.domain.Restaurant;
import org.example.loader.CsvRestaurantLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvRestaurantLoader")
class CsvRestaurantLoaderTest {

    private final CsvRestaurantLoader loader = new CsvRestaurantLoader(
            new ClassPathResource("restaurants.csv"),
            new ClassPathResource("cuisines.csv"));

    @Test
    @DisplayName("loads restaurants from classpath CSV")
    void loadAll_returnsNonEmptyList() {
        List<Restaurant> restaurants = loader.loadAll();

        assertNotNull(restaurants);
        assertFalse(restaurants.isEmpty(), "restaurants.csv should be loaded and produce at least one restaurant");
    }

    @Test
    @DisplayName("resolves cuisine names from cuisines.csv")
    void loadAll_resolvesCuisineNames() {
        List<Restaurant> restaurants = loader.loadAll();

        long withUnknown = restaurants.stream().filter(restaurant -> "Unknown".equals(restaurant.cuisine())).count();
        assertEquals(0, withUnknown, "all restaurants should have cuisine name resolved (none should be 'Unknown')");

        boolean hasKnownCuisine = restaurants.stream()
                .anyMatch(restaurant -> "American".equals(restaurant.cuisine()) || "Chinese".equals(restaurant.cuisine()) || "Spanish".equals(restaurant.cuisine()));
        assertTrue(hasKnownCuisine, "at least one restaurant should have a known cuisine from cuisines.csv");
    }

    @Test
    @DisplayName("every loaded restaurant has required fields set")
    void loadAll_everyRestaurantHasRequiredFields() {
        List<Restaurant> restaurants = loader.loadAll();

        for (Restaurant restaurant : restaurants) {
            assertNotNull(restaurant.name(), "name should not be null");
            assertFalse(restaurant.name().isBlank(), "name should not be blank");
            assertNotNull(restaurant.cuisine(), "cuisine should not be null");
            assertFalse(restaurant.cuisine().isBlank(), "cuisine should not be blank");
        }
    }

    @Test
    @DisplayName("loadAll returns cached data on second call")
    void loadAll_secondCall_returnsCachedData() {
        List<Restaurant> first = loader.loadAll();
        List<Restaurant> second = loader.loadAll();

        assertEquals(first, second);
    }

    @Test
    @DisplayName("skips malformed CSV lines and parses valid ones")
    void loadAll_malformedLines_skipsInvalid() {
        CsvRestaurantLoader malformedLoader = new CsvRestaurantLoader(
                new ClassPathResource("loader/malformed-restaurants.csv"),
                new ClassPathResource("loader/test-cuisines.csv"));
        List<Restaurant> restaurants = malformedLoader.loadAll();

        assertEquals(3, restaurants.size());
        assertEquals("Good Place", restaurants.get(0).name());
        assertEquals("American", restaurants.get(0).cuisine());
        assertEquals("Good2", restaurants.get(2).name());
    }

    @Test
    @DisplayName("resolves cuisine to Unknown when cuisines file is missing")
    void loadAll_missingCuisinesFile_resolvesToUnknown() {
        CsvRestaurantLoader noCuisinesLoader = new CsvRestaurantLoader(
                new ClassPathResource("loader/test-restaurants.csv"),
                new ClassPathResource("nonexistent.csv"));
        List<Restaurant> restaurants = noCuisinesLoader.loadAll();

        assertFalse(restaurants.isEmpty());
        assertTrue(restaurants.stream().allMatch(r -> "Unknown".equals(r.cuisine())));
    }

    @Test
    @DisplayName("returns empty list when restaurants file is missing")
    void loadAll_missingRestaurantsFile_returnsEmpty() {
        CsvRestaurantLoader missingLoader = new CsvRestaurantLoader(
                new ClassPathResource("nonexistent.csv"),
                new ClassPathResource("loader/test-cuisines.csv"));
        List<Restaurant> restaurants = missingLoader.loadAll();

        assertTrue(restaurants.isEmpty());
    }

    // --- Resource abstraction tests ---

    @Test
    @DisplayName("loads from ByteArrayResource (in-memory CSV)")
    void loadAll_byteArrayResource_parsesCorrectly() {
        String cuisinesCsv = "id,name\n1,Italian\n";
        String restaurantsCsv = "name,customer_rating,distance,price,cuisine_id\nPasta House,5,1.0,20,1\n";

        CsvRestaurantLoader inMemoryLoader = new CsvRestaurantLoader(
                new ByteArrayResource(restaurantsCsv.getBytes(StandardCharsets.UTF_8)),
                new ByteArrayResource(cuisinesCsv.getBytes(StandardCharsets.UTF_8)));
        List<Restaurant> restaurants = inMemoryLoader.loadAll();

        assertEquals(1, restaurants.size());
        assertEquals("Pasta House", restaurants.get(0).name());
        assertEquals("Italian", restaurants.get(0).cuisine());
        assertEquals(5, restaurants.get(0).customerRating());
    }

    @Test
    @DisplayName("loads from FileSystemResource (temp file on disk)")
    void loadAll_fileSystemResource_parsesCorrectly(@TempDir Path tempDir) throws IOException {
        Path cuisinesFile = tempDir.resolve("cuisines.csv");
        Files.writeString(cuisinesFile, "id,name\n1,Mexican\n");

        Path restaurantsFile = tempDir.resolve("restaurants.csv");
        Files.writeString(restaurantsFile, "name,customer_rating,distance,price,cuisine_id\nTaco Place,4,2.5,15,1\n");

        CsvRestaurantLoader fsLoader = new CsvRestaurantLoader(
                new FileSystemResource(restaurantsFile),
                new FileSystemResource(cuisinesFile));
        List<Restaurant> restaurants = fsLoader.loadAll();

        assertEquals(1, restaurants.size());
        assertEquals("Taco Place", restaurants.get(0).name());
        assertEquals("Mexican", restaurants.get(0).cuisine());
    }

    @Test
    @DisplayName("returns empty list for header-only restaurants CSV")
    void loadAll_headerOnlyRestaurants_returnsEmpty() {
        String cuisinesCsv = "id,name\n1,Thai\n";
        String restaurantsCsv = "name,customer_rating,distance,price,cuisine_id\n";

        CsvRestaurantLoader headerOnlyLoader = new CsvRestaurantLoader(
                new ByteArrayResource(restaurantsCsv.getBytes(StandardCharsets.UTF_8)),
                new ByteArrayResource(cuisinesCsv.getBytes(StandardCharsets.UTF_8)));
        List<Restaurant> restaurants = headerOnlyLoader.loadAll();

        assertTrue(restaurants.isEmpty());
    }

    @Test
    @DisplayName("returns empty list for completely empty restaurants file")
    void loadAll_emptyRestaurantsFile_returnsEmpty() {
        CsvRestaurantLoader emptyLoader = new CsvRestaurantLoader(
                new ByteArrayResource(new byte[0]),
                new ByteArrayResource("id,name\n1,Thai\n".getBytes(StandardCharsets.UTF_8)));
        List<Restaurant> restaurants = emptyLoader.loadAll();

        assertTrue(restaurants.isEmpty());
    }

    @Test
    @DisplayName("parses CSV without header row (first row is data)")
    void loadAll_noHeaderRow_parsesAllRows() {
        String cuisinesCsv = "1,Japanese\n";
        String restaurantsCsv = "Sushi Bar,5,0.5,30,1\nRamen Shop,4,1.0,15,1\n";

        CsvRestaurantLoader noHeaderLoader = new CsvRestaurantLoader(
                new ByteArrayResource(restaurantsCsv.getBytes(StandardCharsets.UTF_8)),
                new ByteArrayResource(cuisinesCsv.getBytes(StandardCharsets.UTF_8)));
        List<Restaurant> restaurants = noHeaderLoader.loadAll();

        assertEquals(2, restaurants.size());
        assertEquals("Sushi Bar", restaurants.get(0).name());
        assertEquals("Ramen Shop", restaurants.get(1).name());
    }

    @Test
    @DisplayName("resolves unmapped cuisine IDs to Unknown")
    void loadAll_unmappedCuisineId_resolvesToUnknown() {
        String cuisinesCsv = "id,name\n1,Korean\n";
        String restaurantsCsv = "name,customer_rating,distance,price,cuisine_id\n"
                + "Known,5,1.0,10,1\n"
                + "Mystery,4,2.0,15,99\n";

        CsvRestaurantLoader loader = new CsvRestaurantLoader(
                new ByteArrayResource(restaurantsCsv.getBytes(StandardCharsets.UTF_8)),
                new ByteArrayResource(cuisinesCsv.getBytes(StandardCharsets.UTF_8)));
        List<Restaurant> restaurants = loader.loadAll();

        assertEquals(2, restaurants.size());
        assertEquals("Korean", restaurants.get(0).cuisine());
        assertEquals("Unknown", restaurants.get(1).cuisine());
    }
}
