package org.example.loader;

import org.example.domain.Restaurant;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads restaurants from restaurants.csv and resolves cuisine names from cuisines.csv.
 * Format: name,customer_rating,distance,price,cuisine_id (header on first line).
 */
@Component
public class CsvRestaurantLoader implements RestaurantLoader {

    private static final String RESTAURANTS_RESOURCE = "restaurants.csv";
    private static final String CUISINES_RESOURCE = "cuisines.csv";

    private List<Restaurant> cache;

    @Override
    public synchronized List<Restaurant> loadAll() {
        if (cache != null) {
            return cache;
        }
        Map<Integer, String> cuisines = loadCuisines();
        cache = parseRestaurants(cuisines);
        return cache;
    }

    private Map<Integer, String> loadCuisines() {
        Map<Integer, String> map = new HashMap<>();
        try (var input = new ClassPathResource(CUISINES_RESOURCE).getInputStream();
             var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (first && line.toLowerCase().startsWith("id,")) {
                    first = false;
                    continue;
                }
                first = false;
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        map.put(id, name);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            // No cuisines file: continue without names
        }
        return map;
    }

    private List<Restaurant> parseRestaurants(Map<Integer, String> cuisines) {
        List<Restaurant> result = new ArrayList<>();
        try (var input = new ClassPathResource(RESTAURANTS_RESOURCE).getInputStream();
             var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (first && line.toLowerCase().startsWith("name,")) {
                    first = false;
                    continue;
                }
                first = false;
                Restaurant restaurant = parseLine(line, cuisines);
                if (restaurant != null) result.add(restaurant);
            }
        } catch (Exception e) {
            return List.of();
        }
        return result;
    }

    private Restaurant parseLine(String line, Map<Integer, String> cuisines) {
        String[] parts = line.split(",", -1);
        if (parts.length < 5) return null;
        try {
            String name = parts[0].trim();
            int rating = Integer.parseInt(parts[1].trim());
            double distance = Double.parseDouble(parts[2].trim());
            int price = Integer.parseInt(parts[3].trim());
            int cuisineId = Integer.parseInt(parts[4].trim());
            String cuisine = cuisines.getOrDefault(cuisineId, "Unknown");
            return new Restaurant(name, rating, distance, price, cuisine);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
