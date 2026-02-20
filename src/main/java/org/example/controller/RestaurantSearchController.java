package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.domain.Restaurant;
import org.example.domain.SearchCriteria;
import org.example.service.RestaurantSearchService;
import org.example.validation.SearchCriteriaValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurant Search", description = "Search restaurants by name, rating, distance, price, and cuisine")
public class RestaurantSearchController {

    private final RestaurantSearchService searchService;
    private final SearchCriteriaValidator validator;

    public RestaurantSearchController(RestaurantSearchService searchService, SearchCriteriaValidator validator) {
        this.searchService = searchService;
        this.validator = validator;
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants", description = "Returns up to 5 best matches. All parameters are optional. Invalid values return 400.")
    public ResponseEntity<?> search(
            @Parameter(description = "Restaurant name (exact or partial match)") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Minimum customer rating (1-5 stars)") @RequestParam(value = "customerRating", required = false) Integer customerRating,
            @Parameter(description = "Maximum distance in miles (1-10)") @RequestParam(value = "distance", required = false) Double distance,
            @Parameter(description = "Maximum price per person ($10-$50)") @RequestParam(value = "price", required = false) Integer price,
            @Parameter(description = "Cuisine (exact or partial match)") @RequestParam(value = "cuisine", required = false) String cuisine) {

        SearchCriteria criteria = new SearchCriteria(name, customerRating, distance, price, cuisine);
        var error = validator.validate(criteria);
        if (error.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", error.get()));
        }
        List<Restaurant> results = searchService.search(criteria);
        return ResponseEntity.ok(results);
    }
}
