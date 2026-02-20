package org.example.service;

import org.example.domain.Restaurant;
import org.example.domain.SearchCriteria;
import org.example.loader.RestaurantLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orchestrates restaurant search: load data, filter by criteria, sort, return top N (configurable).
 */
@Service
public class RestaurantSearchService {

    private final RestaurantLoader loader;
    private final int maxResults;

    public RestaurantSearchService(RestaurantLoader loader,
                                  @Value("${restaurant.search.max-results:5}") int maxResults) {
        this.loader = loader;
        this.maxResults = maxResults;
    }

    /**
     * Returns up to max-results best-matching restaurants for the given criteria, or empty list if none match.
     * Restaurants with null or blank name are excluded so every result at least contains the restaurant name.
     */
    public List<Restaurant> search(SearchCriteria criteria) {
        List<Restaurant> all = loader.loadAll();
        Stream<Restaurant> stream = all.stream()
                .filter(RestaurantSearchService::hasName)
                .filter(restaurant -> RestaurantMatcher.matches(restaurant, criteria));
        List<Restaurant> matched = stream.collect(Collectors.toList());
        List<Restaurant> sorted = RestaurantSorter.sortByBestMatch(matched);
        return sorted.stream().limit(maxResults).collect(Collectors.toList());
    }

    private static boolean hasName(Restaurant restaurant) {
        return restaurant.getName() != null && !restaurant.getName().isBlank();
    }
}
