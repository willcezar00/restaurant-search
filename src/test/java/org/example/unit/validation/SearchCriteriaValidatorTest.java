package org.example.unit.validation;

import org.example.domain.SearchCriteria;
import org.example.validation.SearchCriteriaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SearchCriteriaValidator")
class SearchCriteriaValidatorTest {

    private SearchCriteriaValidator validator;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        validator = new SearchCriteriaValidator(messageSource, 1, 5, 1, 10, 10, 50);
    }

    @Test
    @DisplayName("valid criteria returns empty list")
    void validate_validCriteria_returnsEmpty() {
        assertTrue(validator.validate(new SearchCriteria(null, null, null, null, null)).isEmpty());
        assertTrue(validator.validate(new SearchCriteria("x", 3, 5.0, 25, "American")).isEmpty());
    }

    @Test
    @DisplayName("customerRating below min returns error")
    void validate_ratingBelowMin_returnsError() {
        var result = validator.validate(new SearchCriteria(null, 0, null, null, null));
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("customerRating"));
        assertTrue(result.get(0).contains("1"));
        assertTrue(result.get(0).contains("5"));
    }

    @Test
    @DisplayName("customerRating above max returns error")
    void validate_ratingAboveMax_returnsError() {
        var result = validator.validate(new SearchCriteria(null, 6, null, null, null));
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("customerRating"));
    }

    @Test
    @DisplayName("distance below min returns error")
    void validate_distanceBelowMin_returnsError() {
        var result = validator.validate(new SearchCriteria(null, null, 0.5, null, null));
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("distance"));
    }

    @Test
    @DisplayName("distance above max returns error")
    void validate_distanceAboveMax_returnsError() {
        var result = validator.validate(new SearchCriteria(null, null, 11.0, null, null));
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("distance"));
    }

    @Test
    @DisplayName("price below min returns error")
    void validate_priceBelowMin_returnsError() {
        var result = validator.validate(new SearchCriteria(null, null, null, 9, null));
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("price"));
        assertTrue(result.get(0).contains("10"));
        assertTrue(result.get(0).contains("50"));
    }

    @Test
    @DisplayName("price above max returns error")
    void validate_priceAboveMax_returnsError() {
        var result = validator.validate(new SearchCriteria(null, null, null, 51, null));
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).contains("price"));
    }

    @Test
    @DisplayName("multiple invalid criteria returns all errors")
    void validate_multipleInvalid_returnsAllErrors() {
        var result = validator.validate(new SearchCriteria(null, 0, 0.5, 9, null));
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(s -> s.contains("customerRating")));
        assertTrue(result.stream().anyMatch(s -> s.contains("distance")));
        assertTrue(result.stream().anyMatch(s -> s.contains("price")));
    }
}
