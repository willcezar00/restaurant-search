package org.example.domain;

import java.util.Objects;

/**
 * Domain model for a restaurant.
 */
public class Restaurant {

    private final String name;
    private final int customerRating;
    private final double distance;
    private final int price;
    private final String cuisine;

    public Restaurant(String name, int customerRating, double distance, int price, String cuisine) {
        this.name = name;
        this.customerRating = customerRating;
        this.distance = distance;
        this.price = price;
        this.cuisine = cuisine;
    }

    public String getName() {
        return name;
    }

    public int getCustomerRating() {
        return customerRating;
    }

    public double getDistance() {
        return distance;
    }

    public int getPrice() {
        return price;
    }

    public String getCuisine() {
        return cuisine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return customerRating == that.customerRating
                && Double.compare(that.distance, distance) == 0
                && price == that.price
                && Objects.equals(name, that.name)
                && Objects.equals(cuisine, that.cuisine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, customerRating, distance, price, cuisine);
    }
}
