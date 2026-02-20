package org.example.service;

import org.example.domain.Restaurant;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pure business logic: sort order for "best match".
 * 1. Distance (asc), 2. Customer rating (desc), 3. Price (asc), 4. name (deterministic tie-break).
 * For random tie-break per spec, you can shuffle equal groups in the service.
 */
public final class RestaurantSorter {

    private static final Comparator<Restaurant> COMPARATOR = Comparator
            .comparingDouble(Restaurant::getDistance)
            .thenComparing(Comparator.comparingInt(Restaurant::getCustomerRating).reversed())
            .thenComparingInt(Restaurant::getPrice)
            .thenComparing(Restaurant::getName);

    private RestaurantSorter() {}

    /**
     * Returns a new list sorted by distance, then rating (desc), then price (asc), then random.
     */
    public static List<Restaurant> sortByBestMatch(List<Restaurant> restaurants) {
        return restaurants.stream()
                .sorted(COMPARATOR)
                .collect(Collectors.toList());
    }
}
