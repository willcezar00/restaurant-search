package org.example.service;

import org.example.domain.Restaurant;
import org.example.domain.SearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Determines if a restaurant matches the given criteria. Injected into {@link RestaurantSearchService}.
 */
@Component
public final class RestaurantMatcher {

    private final List<BiFunction<Restaurant, SearchCriteria, Boolean>> predicates;

    public RestaurantMatcher() {
        this.predicates = List.of(
                RestaurantMatcher::matchesNameByCriteria,
                RestaurantMatcher::matchesCustomerRating,
                RestaurantMatcher::matchesDistance,
                RestaurantMatcher::matchesPrice,
                RestaurantMatcher::matchesCuisineByCriteria
        );
    }

    /**
     * Returns true if the restaurant matches all non-null criteria (AND
     * relationship).
     */
    public boolean matches(Restaurant restaurant, SearchCriteria criteria) {
      return this.predicates.stream()
              .allMatch(predicate -> predicate.apply(restaurant, criteria));
    }

    /**
     * Case-insensitive partial string match. Empty search means "no filter"
     * (matches).
     */
    private static boolean matchesPartial(String value, String search) {
        if (!StringUtils.hasText(search))
            return true;
        return value != null && value.toLowerCase().contains(search.toLowerCase().trim());
    }

    /** Exact or partial string match (e.g. "Mcd" matches "McDonald's"). Used by predicates. */
    private static boolean matchesNameByCriteria(Restaurant restaurant, SearchCriteria criteria) {
        return matchesPartial(restaurant.name(), criteria.getName());
    }

    /** Exact or partial string match (e.g. "Chi" matches "Chinese"). Used by predicates. */
    private static boolean matchesCuisineByCriteria(Restaurant restaurant, SearchCriteria criteria) {
        return matchesPartial(restaurant.cuisine(), criteria.getCuisine());
    }

    /** Rating match: restaurant rating must be >= requested (e.g. "3" matches 3, 4, 5 stars). */
    private static boolean matchesCustomerRating(Restaurant restaurant, SearchCriteria criteria) {
        if (criteria.getCustomerRating() == null) return true;
        return restaurant.customerRating() >= criteria.getCustomerRating();
    }

    /** Distance match: restaurant distance must be <= requested (e.g. "2" matches 1 or 2 miles). */
    private static boolean matchesDistance(Restaurant restaurant, SearchCriteria criteria) {
       if (criteria.getDistance() == null) return true;
       return restaurant.distance() <= criteria.getDistance();
    }

    /** Price match: restaurant price must be <= requested (e.g. "15" matches $10 or $15). */
    private static boolean matchesPrice(Restaurant restaurant, SearchCriteria criteria) {
        if (criteria.getPrice() == null) return true;
        return restaurant.price() <= criteria.getPrice();
    }
}
