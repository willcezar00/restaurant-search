package org.example.unit.service;

import org.example.domain.Restaurant;
import org.example.domain.SearchCriteria;
import org.example.service.RestaurantMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestaurantMatcher")
class RestaurantMatcherTest {

    private static Restaurant restaurant(String name, int rating, double distance, int price, String cuisine) {
        return new Restaurant(name, rating, distance, price, cuisine);
    }

    @Test
    @DisplayName("matches when criteria is empty")
    void matchesWhenCriteriaEmpty() {
        Restaurant restaurant = restaurant("McDonald's", 4, 1.5, 10, "American");
        assertTrue(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, null, null, null)));
    }

    @Test
    @DisplayName("name: partial match")
    void namePartialMatch() {
        Restaurant restaurant = restaurant("McDonald's", 4, 1.0, 10, "American");
        assertTrue(RestaurantMatcher.matchesName(restaurant.getName(), "Mcd"));
        assertTrue(RestaurantMatcher.matchesName(restaurant.getName(), "mcdonald"));
        assertFalse(RestaurantMatcher.matchesName(restaurant.getName(), "KFC"));
    }

    @Test
    @DisplayName("rating: restaurant must be >= requested")
    void ratingAtLeastRequested() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 10, "American");
        assertTrue(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, 3, null, null, null)));
        assertTrue(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, 4, null, null, null)));
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, 5, null, null, null)));
    }

    @Test
    @DisplayName("distance: restaurant must be <= requested")
    void distanceAtMostRequested() {
        Restaurant restaurant = restaurant("R", 4, 2.0, 10, "American");
        assertTrue(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, 3.0, null, null)));
        assertTrue(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, 2.0, null, null)));
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, 1.0, null, null)));
    }

    @Test
    @DisplayName("price: restaurant must be <= requested")
    void priceAtMostRequested() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 15, "American");
        assertTrue(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, null, 20, null)));
        assertTrue(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, null, 15, null)));
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, null, 10, null)));
    }

    @Test
    @DisplayName("cuisine: partial match")
    void cuisinePartialMatch() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 10, "Chinese");
        assertTrue(RestaurantMatcher.matchesCuisine(restaurant.getCuisine(), "Chi"));
        assertTrue(RestaurantMatcher.matchesCuisine(restaurant.getCuisine(), "chinese"));
        assertFalse(RestaurantMatcher.matchesCuisine(restaurant.getCuisine(), "Thai"));
    }

    // --- matches(Restaurant, SearchCriteria) ---

    @Test
    @DisplayName("matches: all criteria set and satisfied returns true")
    void matches_allCriteriaMatch_returnsTrue() {
        Restaurant restaurant = restaurant("McDonald's", 4, 2.0, 15, "American");
        SearchCriteria c = new SearchCriteria("Mcd", 3, 3.0, 20, "Amer");
        assertTrue(RestaurantMatcher.matches(restaurant, c));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when name does not match")
    void matches_failsWhenNameDoesNotMatch() {
        Restaurant restaurant = restaurant("KFC", 4, 1.0, 10, "American");
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria("Mcd", null, null, null, null)));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when rating too low")
    void matches_failsWhenRatingTooLow() {
        Restaurant restaurant = restaurant("R", 2, 1.0, 10, "American");
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, 3, null, null, null)));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when distance too far")
    void matches_failsWhenDistanceTooFar() {
        Restaurant restaurant = restaurant("R", 4, 5.0, 10, "American");
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, 2.0, null, null)));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when price too high")
    void matches_failsWhenPriceTooHigh() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 30, "American");
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, null, 20, null)));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when cuisine does not match")
    void matches_failsWhenCuisineDoesNotMatch() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 10, "Thai");
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria(null, null, null, null, "Chinese")));
    }

    @Test
    @DisplayName("matches: combined criteria (name + distance) both must match")
    void matches_combinedCriteria() {
        Restaurant restaurant = restaurant("Delicious Food", 4, 2.0, 15, "American");
        assertTrue(RestaurantMatcher.matches(restaurant, new SearchCriteria("Delicious", 4, 3.0, null, null)));
        assertFalse(RestaurantMatcher.matches(restaurant, new SearchCriteria("Delicious", 4, 1.0, null, null)));
    }

    // --- matchesName / matchesCuisine edge cases (matchesPartial behavior) ---

    @Test
    @DisplayName("matchesName: null or blank search means no filter (matches)")
    void matchesName_blankSearch_returnsTrue() {
        assertTrue(RestaurantMatcher.matchesName("McDonald's", null));
        assertTrue(RestaurantMatcher.matchesName("McDonald's", ""));
        assertTrue(RestaurantMatcher.matchesName("McDonald's", "   "));
    }

    @Test
    @DisplayName("matchesName: null value does not match non-blank search")
    void matchesName_nullValue_returnsFalseWhenSearchPresent() {
        assertFalse(RestaurantMatcher.matchesName(null, "Mcd"));
    }

    @Test
    @DisplayName("matchesCuisine: null or blank search means no filter (matches)")
    void matchesCuisine_blankSearch_returnsTrue() {
        assertTrue(RestaurantMatcher.matchesCuisine("Chinese", null));
        assertTrue(RestaurantMatcher.matchesCuisine("Chinese", ""));
    }

    @Test
    @DisplayName("matchesCuisine: null value does not match non-blank search")
    void matchesCuisine_nullValue_returnsFalseWhenSearchPresent() {
        assertFalse(RestaurantMatcher.matchesCuisine(null, "Chi"));
    }
}
