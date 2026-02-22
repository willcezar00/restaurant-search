package org.example.unit.service;

import org.example.domain.Restaurant;
import org.example.service.RestaurantSorter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestaurantSorter")
class RestaurantSorterTest {

    private static Restaurant r(String name, int rating, double distance, int price, String cuisine) {
        return new Restaurant(name, rating, distance, price, cuisine);
    }

    private static List<Restaurant> sortWithComparator(List<Restaurant> input) {
        return input.stream().sorted(RestaurantSorter.bestMatchComparator()).collect(Collectors.toList());
    }

    @Test
    @DisplayName("sorts by distance ascending first")
    void bestMatchComparator_distanceFirst() {
        List<Restaurant> input = List.of(
                r("Far", 5, 5.0, 10, "A"),
                r("Near", 5, 1.0, 10, "A"),
                r("Mid", 5, 3.0, 10, "A"));
        List<Restaurant> sorted = sortWithComparator(input);

        assertEquals("Near", sorted.get(0).name());
        assertEquals("Mid", sorted.get(1).name());
        assertEquals("Far", sorted.get(2).name());
    }

    @Test
    @DisplayName("when distance equal, sorts by rating descending")
    void bestMatchComparator_thenByRatingDesc() {
        List<Restaurant> input = List.of(
                r("Low", 2, 1.0, 10, "A"),
                r("High", 5, 1.0, 10, "A"),
                r("Mid", 3, 1.0, 10, "A"));
        List<Restaurant> sorted = sortWithComparator(input);

        assertEquals("High", sorted.get(0).name());
        assertEquals("Mid", sorted.get(1).name());
        assertEquals("Low", sorted.get(2).name());
    }

    @Test
    @DisplayName("when distance and rating equal, sorts by price ascending")
    void bestMatchComparator_thenByPriceAsc() {
        List<Restaurant> input = List.of(
                r("Expensive", 5, 1.0, 50, "A"),
                r("Cheap", 5, 1.0, 10, "A"),
                r("Mid", 5, 1.0, 25, "A"));
        List<Restaurant> sorted = sortWithComparator(input);

        assertEquals("Cheap", sorted.get(0).name());
        assertEquals("Mid", sorted.get(1).name());
        assertEquals("Expensive", sorted.get(2).name());
    }

    @Test
    @DisplayName("when distance, rating and price equal, sorts by name")
    void bestMatchComparator_thenByName() {
        List<Restaurant> input = List.of(
                r("Zebra", 5, 1.0, 10, "A"),
                r("Alpha", 5, 1.0, 10, "A"),
                r("Middle", 5, 1.0, 10, "A"));
        List<Restaurant> sorted = sortWithComparator(input);

        assertEquals("Alpha", sorted.get(0).name());
        assertEquals("Middle", sorted.get(1).name());
        assertEquals("Zebra", sorted.get(2).name());
    }

    @Test
    @DisplayName("returns new list without modifying input")
    void bestMatchComparator_doesNotMutateInput() {
        List<Restaurant> input = List.of(r("A", 3, 2.0, 15, "X"));
        List<Restaurant> sorted = sortWithComparator(input);

        assertNotSame(input, sorted);
        assertEquals(1, input.size());
        assertEquals(1, sorted.size());
    }

    @Test
    @DisplayName("empty list returns empty list")
    void bestMatchComparator_emptyList_returnsEmpty() {
        List<Restaurant> sorted = sortWithComparator(List.of());
        assertTrue(sorted.isEmpty());
    }

    @Test
    @DisplayName("single element returns list with that element")
    void bestMatchComparator_singleElement_returnsSameOrder() {
        List<Restaurant> input = List.of(r("Only", 3, 2.0, 15, "X"));
        List<Restaurant> sorted = sortWithComparator(input);
        assertEquals(1, sorted.size());
        assertEquals("Only", sorted.get(0).name());
    }
}
