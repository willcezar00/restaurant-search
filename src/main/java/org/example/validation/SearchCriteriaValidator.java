package org.example.validation;

import org.example.domain.SearchCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * Validates search criteria. Returns a list of error messages when invalid, empty list when valid.
 * Bounds are configurable via application.properties (restaurant.search.criteria.*).
 * Messages are resolved from message keys for internationalization.
 */
@Component
public class SearchCriteriaValidator {

    private static final String MSG_RATING_RANGE = "validation.criteria.rating.range";
    private static final String MSG_DISTANCE_RANGE = "validation.criteria.distance.range";
    private static final String MSG_PRICE_RANGE = "validation.criteria.price.range";

    private final MessageSource messageSource;
    private final int ratingMin;
    private final int ratingMax;
    private final double distanceMin;
    private final double distanceMax;
    private final int priceMin;
    private final int priceMax;
    private final List<Function<SearchCriteria, Optional<ValidationError>>> validators;

    public SearchCriteriaValidator(
            MessageSource messageSource,
            @Value("${restaurant.search.criteria.rating-min:1}") int ratingMin,
            @Value("${restaurant.search.criteria.rating-max:5}") int ratingMax,
            @Value("${restaurant.search.criteria.distance-min:1}") double distanceMin,
            @Value("${restaurant.search.criteria.distance-max:10}") double distanceMax,
            @Value("${restaurant.search.criteria.price-min:10}") int priceMin,
            @Value("${restaurant.search.criteria.price-max:50}") int priceMax) {
        this.messageSource = messageSource;
        this.ratingMin = ratingMin;
        this.ratingMax = ratingMax;
        this.distanceMin = distanceMin;
        this.distanceMax = distanceMax;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.validators = List.of(
                this::validRating,
                this::validDistance,
                this::validPrice
        );
    }

    /**
     * Validates the given criteria. Returns an empty list if valid, or all localized error messages if invalid.
     * Locale is taken from LocaleContextHolder (e.g. set from Accept-Language).
     */
    public List<String> validate(SearchCriteria criteria) {
        Locale locale = LocaleContextHolder.getLocale();
        return validators.stream()
                .map(v -> v.apply(criteria))
                .flatMap(Optional::stream)
                .map(err -> message(err.messageKey(), locale, err.args()))
                .toList();
    }

    private String message(String code, Locale locale, Object[] args) {
        return messageSource.getMessage(code, args, locale);
    }

    private Optional<ValidationError> validRating(SearchCriteria criteria) {
        if (criteria.getCustomerRating() != null
                && (criteria.getCustomerRating() < ratingMin || criteria.getCustomerRating() > ratingMax)) {
            return Optional.of(ValidationError.of(MSG_RATING_RANGE, ratingMin, ratingMax));
        }
        return Optional.empty();
    }

    private Optional<ValidationError> validDistance(SearchCriteria criteria) {
        if (criteria.getDistance() != null
                && (criteria.getDistance() < distanceMin || criteria.getDistance() > distanceMax)) {
            return Optional.of(ValidationError.of(MSG_DISTANCE_RANGE, distanceMin, distanceMax));
        }
        return Optional.empty();
    }

    private Optional<ValidationError> validPrice(SearchCriteria criteria) {
        if (criteria.getPrice() != null
                && (criteria.getPrice() < priceMin || criteria.getPrice() > priceMax)) {
            return Optional.of(ValidationError.of(MSG_PRICE_RANGE, priceMin, priceMax));
        }
        return Optional.empty();
    }

}
