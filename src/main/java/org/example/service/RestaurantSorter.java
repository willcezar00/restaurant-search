package org.example.service;

import org.example.domain.Restaurant;

import java.util.Comparator;

/**
 * Defines sort order for "best match": distance (asc), rating (desc), price (asc), name (tie-break).
 * Use {@link #bestMatchComparator()} in a stream pipeline (filter, sorted, limit) to avoid intermediate collections.
 */
public final class RestaurantSorter {

    private static final Comparator<Restaurant> BEST_MATCH_ORDER = Comparator
            .comparingDouble(Restaurant::distance)
            .thenComparing(Comparator.comparingInt(Restaurant::customerRating).reversed())
            .thenComparingInt(Restaurant::price)
            .thenComparing(Restaurant::name);

    private RestaurantSorter() {}

    /**
     * Returns the comparator for best-match order. Use with stream.sorted(comparator).
     */
    public static Comparator<Restaurant> bestMatchComparator() {
        return BEST_MATCH_ORDER;
    }
}
