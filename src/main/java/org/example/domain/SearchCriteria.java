package org.example.domain;

import java.util.Objects;

/**
 * Search criteria for restaurant search. All fields are optional (null = not specified).
 */
public class SearchCriteria {

    private final String name;
    private final Integer customerRating;
    private final Double distance;
    private final Integer price;
    private final String cuisine;

    public SearchCriteria(String name, Integer customerRating, Double distance, Integer price, String cuisine) {
        this.name = name;
        this.customerRating = customerRating;
        this.distance = distance;
        this.price = price;
        this.cuisine = cuisine;
    }

    public String getName() {
        return name;
    }

    public Integer getCustomerRating() {
        return customerRating;
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getPrice() {
        return price;
    }

    public String getCuisine() {
        return cuisine;
    }

    public boolean isEmpty() {
        return name == null && customerRating == null && distance == null && price == null && cuisine == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchCriteria that = (SearchCriteria) o;
        return Objects.equals(name, that.name)
                && Objects.equals(customerRating, that.customerRating)
                && Objects.equals(distance, that.distance)
                && Objects.equals(price, that.price)
                && Objects.equals(cuisine, that.cuisine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, customerRating, distance, price, cuisine);
    }
}
