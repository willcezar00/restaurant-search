package org.example.validation;

import org.example.domain.SearchCriteria;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validates search criteria. Returns an error message when invalid, empty when valid.
 */
@Component
public class SearchCriteriaValidator {

    private static final int RATING_MIN = 1;
    private static final int RATING_MAX = 5;
    private static final double DISTANCE_MIN = 1;
    private static final double DISTANCE_MAX = 10;
    private static final int PRICE_MIN = 10;
    private static final int PRICE_MAX = 50;

    /**
     * Validates the given criteria. Returns empty if valid, or the error message if invalid.
     */
    public Optional<String> validate(SearchCriteria criteria) {
        if (criteria.getCustomerRating() != null
                && (criteria.getCustomerRating() < RATING_MIN || criteria.getCustomerRating() > RATING_MAX)) {
            return Optional.of("customerRating must be between " + RATING_MIN + " and " + RATING_MAX);
        }
        if (criteria.getDistance() != null
                && (criteria.getDistance() < DISTANCE_MIN || criteria.getDistance() > DISTANCE_MAX)) {
            return Optional.of("distance must be between " + DISTANCE_MIN + " and " + DISTANCE_MAX + " miles");
        }
        if (criteria.getPrice() != null
                && (criteria.getPrice() < PRICE_MIN || criteria.getPrice() > PRICE_MAX)) {
            return Optional.of("price must be between $" + PRICE_MIN + " and $" + PRICE_MAX);
        }
        return Optional.empty();
    }
}
