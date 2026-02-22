package org.example.integration;

import org.example.domain.Restaurant;
import org.example.loader.RestaurantLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test: full Spring context, API endpoint.
 * Loader is mocked so the test is stable and does not depend on CSV file content.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Restaurant Search API (integration)")
class RestaurantSearchIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RestaurantLoader loader;

    private static Restaurant r(String name, int rating, double distance, int price, String cuisine) {
        return new Restaurant(name, rating, distance, price, cuisine);
    }

    @Test
    @DisplayName("GET /search with no params returns 200 and up to 5 results")
    void search_noParams_returnsOkAndList() throws Exception {
        when(loader.loadAll()).thenReturn(List.of(
                r("Near Spot", 4, 1.0, 15, "American"),
                r("Mid Spot", 3, 2.0, 20, "Chinese"),
                r("Far Spot", 5, 5.0, 10, "Thai")));

        mockMvc.perform(get("/api/restaurants/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Near Spot"))
                .andExpect(jsonPath("$[0].customerRating").value(4))
                .andExpect(jsonPath("$[0].distance").value(1.0))
                .andExpect(jsonPath("$[0].price").value(15))
                .andExpect(jsonPath("$[0].cuisine").value("American"))
                .andExpect(jsonPath("$[1].name").value("Mid Spot"))
                .andExpect(jsonPath("$[2].name").value("Far Spot"));
    }

    @Test
    @DisplayName("GET /search with cuisine filter returns matching restaurants only")
    void search_withCuisineFilter_returnsMatchingOnly() throws Exception {
        when(loader.loadAll()).thenReturn(List.of(
                r("American Diner", 4, 1.0, 15, "American"),
                r("Chinese Place", 3, 2.0, 20, "Chinese")));

        mockMvc.perform(get("/api/restaurants/search").param("cuisine", "American"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("American Diner"))
                .andExpect(jsonPath("$[0].cuisine").value("American"));
    }

    @Test
    @DisplayName("GET /search with name partial match returns matching restaurants")
    void search_withNameFilter_returnsMatchingOnly() throws Exception {
        when(loader.loadAll()).thenReturn(List.of(
                r("Delicious Kitchen", 4, 1.0, 15, "American"),
                r("Other Place", 3, 2.0, 20, "Chinese")));

        mockMvc.perform(get("/api/restaurants/search").param("name", "Delicious"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Delicious Kitchen"));
    }

    @Test
    @DisplayName("GET /search with invalid param returns 400")
    void search_invalidParam_returns400() throws Exception {
        mockMvc.perform(get("/api/restaurants/search").param("customerRating", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}
