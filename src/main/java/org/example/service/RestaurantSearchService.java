package org.example.service;

import org.example.domain.Restaurant;
import org.example.domain.SearchCriteria;
import org.example.loader.RestaurantLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates restaurant search: load data, filter by criteria, sort, return top N (configurable).
 */
@Service
public class RestaurantSearchService {

    private final RestaurantLoader loader;
    private final RestaurantMatcher matcher;
    private final int maxResults;

    public RestaurantSearchService(RestaurantLoader loader,
                                  RestaurantMatcher matcher,
                                  @Value("${restaurant.search.max-results:5}") int maxResults) {
        this.loader = loader;
        this.matcher = matcher;
        this.maxResults = maxResults;
    }

    /**
     * Returns up to max-results best-matching restaurants for the given criteria, or empty list if none match.
     * Restaurants with null or blank name are excluded so every result at least contains the restaurant name.
     */
    public List<Restaurant> search(SearchCriteria criteria) {
        return loader.loadAll().stream()
                .filter(RestaurantSearchService::hasName)
                .filter(restaurant -> matcher.matches(restaurant, criteria))
                .sorted(RestaurantSorter.bestMatchComparator())
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private static boolean hasName(Restaurant restaurant) {
        return StringUtils.hasText(restaurant.name());
    }
}
