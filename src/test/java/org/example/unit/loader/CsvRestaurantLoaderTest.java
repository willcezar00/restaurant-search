package org.example.unit.loader;

import org.example.domain.Restaurant;
import org.example.loader.CsvRestaurantLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvRestaurantLoader")
class CsvRestaurantLoaderTest {

    private final CsvRestaurantLoader loader = new CsvRestaurantLoader();

    @Test
    @DisplayName("loads restaurants from classpath CSV")
    void loadAll_returnsNonEmptyList() {
        List<Restaurant> restaurants = loader.loadAll();

        assertNotNull(restaurants);
        assertFalse(restaurants.isEmpty(), "restaurants.csv should be loaded and produce at least one restaurant");
        // Avoid asserting exact count so test stays valid with different CSV data
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
        // We do not assert business ranges (rating 1-5, distance 1-10, etc.) so the test
        // remains valid even if the CSV data has edge cases or varies per environment.
    }
}
