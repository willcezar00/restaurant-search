package org.example.unit.controller;

import org.example.domain.Restaurant;
import org.example.loader.RestaurantLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RestaurantSearchController")
class RestaurantSearchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RestaurantLoader loader;

    @Test
    @DisplayName("GET /search with no params returns 200 and service result")
    void search_noParams_returnsOkAndList() throws Exception {
        when(loader.loadAll()).thenReturn(List.of(new Restaurant("Test", 4, 1.0, 15, "American")));

        mockMvc.perform(get("/api/restaurants/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test"))
                .andExpect(jsonPath("$[0].customerRating").value(4))
                .andExpect(jsonPath("$[0].cuisine").value("American"));
    }

    @Test
    @DisplayName("GET /search with valid params returns 200")
    void search_validParams_returnsOk() throws Exception {
        when(loader.loadAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/restaurants/search")
                        .param("name", "Delicious")
                        .param("customerRating", "3")
                        .param("distance", "5")
                        .param("price", "25")
                        .param("cuisine", "American"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /search with invalid customerRating returns 400")
    void search_invalidRating_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/restaurants/search").param("customerRating", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("customerRating must be between 1 and 5"));

        mockMvc.perform(get("/api/restaurants/search").param("customerRating", "6"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /search with invalid distance returns 400")
    void search_invalidDistance_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/restaurants/search").param("distance", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("distance must be between 1.0 and 10.0 miles"));

        mockMvc.perform(get("/api/restaurants/search").param("distance", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /search with invalid price returns 400")
    void search_invalidPrice_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/restaurants/search").param("price", "9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("price must be between $10 and $50"));

        mockMvc.perform(get("/api/restaurants/search").param("price", "51"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /search returns empty list when no data")
    void search_noMatches_returnsEmptyList() throws Exception {
        when(loader.loadAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/restaurants/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
