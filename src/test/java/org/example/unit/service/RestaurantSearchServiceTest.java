package org.example.unit.service;

import org.example.domain.Restaurant;
import org.example.domain.SearchCriteria;
import org.example.loader.RestaurantLoader;
import org.example.service.RestaurantSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantSearchService")
class RestaurantSearchServiceTest {

    @Mock
    RestaurantLoader loader;

    private static Restaurant restaurant(String name, int rating, double distance, int price, String cuisine) {
        return new Restaurant(name, rating, distance, price, cuisine);
    }

    @Test
    @DisplayName("returns at most 5 results")
    void search_returnsAtMostFive() {
        List<Restaurant> many = List.of(
                restaurant("A", 5, 1, 10, "American"),
                restaurant("B", 5, 2, 10, "American"),
                restaurant("C", 5, 3, 10, "American"),
                restaurant("D", 5, 4, 10, "American"),
                restaurant("E", 5, 5, 10, "American"),
                restaurant("F", 5, 6, 10, "American"));
        when(loader.loadAll()).thenReturn(many);

        RestaurantSearchService service = new RestaurantSearchService(loader, 5);
        List<Restaurant> results = service.search(new SearchCriteria(null, null, null, null, null));

        assertEquals(5, results.size());
    }

    @Test
    @DisplayName("filters by criteria and returns matching only")
    void search_filtersByCriteria() {
        List<Restaurant> all = List.of(
                restaurant("Low", 2, 1, 10, "American"),
                restaurant("High", 4, 1, 10, "American"));
        when(loader.loadAll()).thenReturn(all);

        RestaurantSearchService service = new RestaurantSearchService(loader, 5);
        List<Restaurant> results = service.search(new SearchCriteria(null, 4, null, null, null));

        assertEquals(1, results.size());
        assertEquals("High", results.get(0).getName());
    }

    @Test
    @DisplayName("returns empty list when no matches")
    void search_noMatches_returnsEmpty() {
        when(loader.loadAll()).thenReturn(List.of(restaurant("Only", 2, 1, 10, "American")));

        RestaurantSearchService service = new RestaurantSearchService(loader, 5);
        List<Restaurant> results = service.search(new SearchCriteria(null, 5, null, null, null));

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("sorts by distance then rating then price")
    void search_sortsByBestMatch() {
        List<Restaurant> all = List.of(
                restaurant("Far", 5, 5.0, 10, "American"),
                restaurant("Near", 3, 1.0, 20, "American"),
                restaurant("Mid", 4, 2.0, 15, "American"));
        when(loader.loadAll()).thenReturn(all);

        RestaurantSearchService service = new RestaurantSearchService(loader, 5);
        List<Restaurant> results = service.search(new SearchCriteria(null, null, null, null, null));

        assertEquals(3, results.size());
        assertEquals("Near", results.get(0).getName());  // distance 1 first
        assertEquals("Mid", results.get(1).getName());   // then 2
        assertEquals("Far", results.get(2).getName());   // then 5
    }

    @Test
    @DisplayName("when loader returns empty list, search returns empty list")
    void search_emptyLoader_returnsEmpty() {
        when(loader.loadAll()).thenReturn(List.of());

        RestaurantSearchService service = new RestaurantSearchService(loader, 5);
        List<Restaurant> results = service.search(new SearchCriteria(null, null, null, null, null));

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("excludes restaurants with null or blank name from results")
    void search_excludesNullOrBlankName() {
        when(loader.loadAll()).thenReturn(List.of(
                restaurant(null, 4, 1.0, 10, "American"),
                restaurant("", 4, 1.0, 10, "American"),
                restaurant("   ", 4, 1.0, 10, "American"),
                restaurant("Valid Name", 4, 1.0, 10, "American")));

        RestaurantSearchService service = new RestaurantSearchService(loader, 5);
        List<Restaurant> results = service.search(new SearchCriteria(null, null, null, null, null));

        assertEquals(1, results.size());
        assertEquals("Valid Name", results.get(0).getName());
    }
}
