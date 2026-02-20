package org.example.service;

import org.example.domain.Restaurant;
import org.example.domain.SearchCriteria;
import org.springframework.util.StringUtils;

/**
 * Pure business logic: determines if a restaurant matches the given criteria.
 * No I/O, no framework dependencies — easy to unit test.
 */
public final class RestaurantMatcher {

    private RestaurantMatcher() {}

    /**
     * Returns true if the restaurant matches all non-null criteria (AND relationship).
     */
    public static boolean matches(Restaurant restaurant, SearchCriteria criteria) {
        if (criteria.isEmpty()) return true;
        if (criteria.getName() != null && !matchesName(restaurant.getName(), criteria.getName())) return false;
        if (criteria.getCustomerRating() != null && restaurant.getCustomerRating() < criteria.getCustomerRating()) return false;
        if (criteria.getDistance() != null && restaurant.getDistance() > criteria.getDistance()) return false;
        if (criteria.getPrice() != null && restaurant.getPrice() > criteria.getPrice()) return false;
        if (criteria.getCuisine() != null && !matchesCuisine(restaurant.getCuisine(), criteria.getCuisine())) return false;
        return true;
    }

    /** Case-insensitive partial string match. Empty search means "no filter" (matches). */
    private static boolean matchesPartial(String value, String search) {
        if (!StringUtils.hasText(search)) return true;
        return value != null && value.toLowerCase().contains(search.toLowerCase().trim());
    }

    /** Exact or partial string match (e.g. "Mcd" matches "McDonald's"). */
    public static boolean matchesName(String restaurantName, String searchName) {
        return matchesPartial(restaurantName, searchName);
    }

    /** Exact or partial string match (e.g. "Chi" matches "Chinese"). */
    public static boolean matchesCuisine(String restaurantCuisine, String searchCuisine) {
        return matchesPartial(restaurantCuisine, searchCuisine);
    }
}
