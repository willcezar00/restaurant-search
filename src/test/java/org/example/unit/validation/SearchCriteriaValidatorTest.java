package org.example.unit.validation;

import org.example.domain.SearchCriteria;
import org.example.validation.SearchCriteriaValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SearchCriteriaValidator")
class SearchCriteriaValidatorTest {

    private final SearchCriteriaValidator validator = new SearchCriteriaValidator();

    @Test
    @DisplayName("valid criteria returns empty")
    void validate_validCriteria_returnsEmpty() {
        assertTrue(validator.validate(new SearchCriteria(null, null, null, null, null)).isEmpty());
        assertTrue(validator.validate(new SearchCriteria("x", 3, 5.0, 25, "American")).isEmpty());
    }

    @Test
    @DisplayName("customerRating below min returns error")
    void validate_ratingBelowMin_returnsError() {
        var result = validator.validate(new SearchCriteria(null, 0, null, null, null));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("customerRating"));
        assertTrue(result.get().contains("1"));
        assertTrue(result.get().contains("5"));
    }

    @Test
    @DisplayName("customerRating above max returns error")
    void validate_ratingAboveMax_returnsError() {
        var result = validator.validate(new SearchCriteria(null, 6, null, null, null));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("customerRating"));
    }

    @Test
    @DisplayName("distance below min returns error")
    void validate_distanceBelowMin_returnsError() {
        var result = validator.validate(new SearchCriteria(null, null, 0.5, null, null));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("distance"));
    }

    @Test
    @DisplayName("distance above max returns error")
    void validate_distanceAboveMax_returnsError() {
        var result = validator.validate(new SearchCriteria(null, null, 11.0, null, null));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("distance"));
    }

    @Test
    @DisplayName("price below min returns error")
    void validate_priceBelowMin_returnsError() {
        var result = validator.validate(new SearchCriteria(null, null, null, 9, null));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("price"));
        assertTrue(result.get().contains("10"));
        assertTrue(result.get().contains("50"));
    }

    @Test
    @DisplayName("price above max returns error")
    void validate_priceAboveMax_returnsError() {
        var result = validator.validate(new SearchCriteria(null, null, null, 51, null));
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("price"));
    }
}
