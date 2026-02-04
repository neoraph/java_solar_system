package com.raph.solarsystem.model;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlanetDataLoaderTest {
    @Test
    void loadsPlanetsFromClasspathJson() {
        List<Planet> planets = PlanetDataLoader.loadFromClasspath("/planets.json");

        assertEquals(8, planets.size());
        assertEquals("Earth", planets.get(2).name());
    }

    @Test
    void throwsOnInvalidColor() {
        String json = """
                [{"name":"X","semiMajorAu":1.0,"eccentricity":0.1,"periodDays":10.0,"inclinationDeg":2.0,"color":"red"}]
                """;

        ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () -> PlanetDataLoader.load(input));
    }
}
