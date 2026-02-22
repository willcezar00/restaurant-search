package org.example.domain;

/**
 * Domain model for a restaurant.
 *
 * @param name          restaurant name
 * @param customerRating 1–5 stars
 * @param distance     distance in miles
 * @param price        price per person
 * @param cuisine      cuisine type
 */
public record Restaurant(
        String name,
        int customerRating,
        double distance,
        int price,
        String cuisine) {

    /** Alias for record accessor so existing getX() callers still work. */
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
}
