package org.example.unit.service;

import org.example.domain.Restaurant;
import org.example.domain.SearchCriteria;
import org.example.service.RestaurantMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestaurantMatcher")
class RestaurantMatcherTest {

    private final RestaurantMatcher matcher = new RestaurantMatcher();

    private static Restaurant restaurant(String name, int rating, double distance, int price, String cuisine) {
        return new Restaurant(name, rating, distance, price, cuisine);
    }

    @Test
    @DisplayName("matches when criteria is empty")
    void matchesWhenCriteriaEmpty() {
        Restaurant restaurant = restaurant("McDonald's", 4, 1.5, 10, "American");
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, null, null, null, null)));
    }

    @Test
    @DisplayName("name: partial match")
    void namePartialMatch() {
        Restaurant restaurant = restaurant("McDonald's", 4, 1.0, 10, "American");
        assertTrue(matcher.matches(restaurant, new SearchCriteria("Mcd", null, null, null, null)));
        assertTrue(matcher.matches(restaurant, new SearchCriteria("mcdonald", null, null, null, null)));
        assertFalse(matcher.matches(restaurant, new SearchCriteria("KFC", null, null, null, null)));
    }

    @Test
    @DisplayName("rating: restaurant must be >= requested")
    void ratingAtLeastRequested() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 10, "American");
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, 3, null, null, null)));
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, 4, null, null, null)));
        assertFalse(matcher.matches(restaurant, new SearchCriteria(null, 5, null, null, null)));
    }

    @Test
    @DisplayName("distance: restaurant must be <= requested")
    void distanceAtMostRequested() {
        Restaurant restaurant = restaurant("R", 4, 2.0, 10, "American");
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, null, 3.0, null, null)));
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, null, 2.0, null, null)));
        assertFalse(matcher.matches(restaurant, new SearchCriteria(null, null, 1.0, null, null)));
    }

    @Test
    @DisplayName("price: restaurant must be <= requested")
    void priceAtMostRequested() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 15, "American");
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, null, null, 20, null)));
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, null, null, 15, null)));
        assertFalse(matcher.matches(restaurant, new SearchCriteria(null, null, null, 10, null)));
    }

    @Test
    @DisplayName("cuisine: partial match")
    void cuisinePartialMatch() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 10, "Chinese");
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, null, null, null, "Chi")));
        assertTrue(matcher.matches(restaurant, new SearchCriteria(null, null, null, null, "chinese")));
        assertFalse(matcher.matches(restaurant, new SearchCriteria(null, null, null, null, "Thai")));
    }

    // --- matches(Restaurant, SearchCriteria) ---

    @Test
    @DisplayName("matches: all criteria set and satisfied returns true")
    void matches_allCriteriaMatch_returnsTrue() {
        Restaurant restaurant = restaurant("McDonald's", 4, 2.0, 15, "American");
        SearchCriteria c = new SearchCriteria("Mcd", 3, 3.0, 20, "Amer");
        assertTrue(matcher.matches(restaurant, c));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when name does not match")
    void matches_failsWhenNameDoesNotMatch() {
        Restaurant restaurant = restaurant("KFC", 4, 1.0, 10, "American");
        assertFalse(matcher.matches(restaurant, new SearchCriteria("Mcd", null, null, null, null)));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when rating too low")
    void matches_failsWhenRatingTooLow() {
        Restaurant restaurant = restaurant("R", 2, 1.0, 10, "American");
        assertFalse(matcher.matches(restaurant, new SearchCriteria(null, 3, null, null, null)));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when distance too far")
    void matches_failsWhenDistanceTooFar() {
        Restaurant restaurant = restaurant("R", 4, 5.0, 10, "American");
        assertFalse(matcher.matches(restaurant, new SearchCriteria(null, null, 2.0, null, null)));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when price too high")
    void matches_failsWhenPriceTooHigh() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 30, "American");
        assertFalse(matcher.matches(restaurant, new SearchCriteria(null, null, null, 20, null)));
    }

    @Test
    @DisplayName("matches: AND relationship - fails when cuisine does not match")
    void matches_failsWhenCuisineDoesNotMatch() {
        Restaurant restaurant = restaurant("R", 4, 1.0, 10, "Thai");
        assertFalse(matcher.matches(restaurant, new SearchCriteria(null, null, null, null, "Chinese")));
    }

    @Test
    @DisplayName("matches: combined criteria (name + distance) both must match")
    void matches_combinedCriteria() {
        Restaurant restaurant = restaurant("Delicious Food", 4, 2.0, 15, "American");
        assertTrue(matcher.matches(restaurant, new SearchCriteria("Delicious", 4, 3.0, null, null)));
        assertFalse(matcher.matches(restaurant, new SearchCriteria("Delicious", 4, 1.0, null, null)));
    }

    // --- name/cuisine edge cases via matches (null or blank search = no filter) ---

    @Test
    @DisplayName("name: null or blank search means no filter (matches)")
    void name_blankSearch_returnsTrue() {
        Restaurant r = restaurant("McDonald's", 4, 1.0, 10, "American");
        assertTrue(matcher.matches(r, new SearchCriteria(null, null, null, null, null)));
        assertTrue(matcher.matches(r, new SearchCriteria("", null, null, null, null)));
        assertTrue(matcher.matches(r, new SearchCriteria("   ", null, null, null, null)));
    }

    @Test
    @DisplayName("name: null restaurant name does not match non-blank search")
    void name_nullValue_returnsFalseWhenSearchPresent() {
        Restaurant r = restaurant(null, 4, 1.0, 10, "American");
        assertFalse(matcher.matches(r, new SearchCriteria("Mcd", null, null, null, null)));
    }

    @Test
    @DisplayName("cuisine: null or blank search means no filter (matches)")
    void cuisine_blankSearch_returnsTrue() {
        Restaurant r = restaurant("R", 4, 1.0, 10, "Chinese");
        assertTrue(matcher.matches(r, new SearchCriteria(null, null, null, null, null)));
        assertTrue(matcher.matches(r, new SearchCriteria(null, null, null, null, "")));
    }

    @Test
    @DisplayName("cuisine: null restaurant cuisine does not match non-blank search")
    void cuisine_nullValue_returnsFalseWhenSearchPresent() {
        Restaurant r = restaurant("R", 4, 1.0, 10, null);
        assertFalse(matcher.matches(r, new SearchCriteria(null, null, null, null, "Chi")));
    }

    @Test
    @DisplayName("cuisine: whitespace-only search means no filter")
    void cuisine_whitespaceOnlySearch_returnsTrue() {
        Restaurant r = restaurant("R", 4, 1.0, 10, "Chinese");
        assertTrue(matcher.matches(r, new SearchCriteria(null, null, null, null, "   ")));
    }

    @Test
    @DisplayName("name: search with leading/trailing spaces is trimmed")
    void name_searchWithSpaces_trimsBeforeMatching() {
        Restaurant r = restaurant("McDonald's", 4, 1.0, 10, "American");
        assertTrue(matcher.matches(r, new SearchCriteria("  mcdonald  ", null, null, null, null)));
    }

    @Test
    @DisplayName("cuisine: search with leading/trailing spaces is trimmed")
    void cuisine_searchWithSpaces_trimsBeforeMatching() {
        Restaurant r = restaurant("R", 4, 1.0, 10, "Chinese");
        assertTrue(matcher.matches(r, new SearchCriteria(null, null, null, null, "  chi  ")));
    }
}
