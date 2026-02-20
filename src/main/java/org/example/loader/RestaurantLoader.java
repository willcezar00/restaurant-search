package org.example.loader;

import org.example.domain.Restaurant;

import java.util.List;

/**
 * Loads restaurant data (e.g. from CSV). Implementations may cache in memory.
 */
public interface RestaurantLoader {

    /**
     * Returns all available restaurants. May be loaded from file at first call and cached.
     */
    List<Restaurant> loadAll();
}
