package org.example.unit;

import org.example.Main;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    @DisplayName("placeholder test to verify test setup runs")
    void testSetupWorks() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Main class is loadable")
    void mainClassExists() {
        assertNotNull(Main.class);
    }
}
