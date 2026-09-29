package com.raph.solarsystem.model;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanetDataLoaderTest {
    @Test
    void loadsPlanetsFromClasspathJson() {
        List<Planet> planets = PlanetDataLoader.loadFromClasspath("/planets.json");

        assertEquals(8, planets.size());
        assertEquals("Earth", planets.get(2).name());
    }

    @Test
    void planetsWithoutMoonsDataDefaultToEmptyList() {
        List<Planet> planets = PlanetDataLoader.loadFromClasspath("/planets.json");

        assertEquals("Mercury", planets.get(0).name());
        assertTrue(planets.get(0).moons().isEmpty());
    }

    @Test
    void earthHasOneMoonWithExpectedName() {
        List<Planet> planets = PlanetDataLoader.loadFromClasspath("/planets.json");
        Planet earth = planets.get(2);

        assertEquals(1, earth.moons().size());
        assertEquals("Moon", earth.moons().get(0).name());
    }

    @Test
    void jupiterHasFourGalileanMoons() {
        List<Planet> planets = PlanetDataLoader.loadFromClasspath("/planets.json");
        Planet jupiter = planets.get(4);

        assertEquals(4, jupiter.moons().size());
    }

    @Test
    void saturnHasItsFiveLargestMoons() {
        List<Planet> planets = PlanetDataLoader.loadFromClasspath("/planets.json");
        Planet saturn = planets.get(5);

        assertEquals(
                List.of("Tethys", "Dione", "Rhea", "Titan", "Iapetus"),
                saturn.moons().stream().map(Moon::name).toList()
        );
    }

    @Test
    void uranusAndNeptuneHaveTheirMajorMoons() {
        List<Planet> planets = PlanetDataLoader.loadFromClasspath("/planets.json");

        assertEquals(
                List.of("Miranda", "Ariel", "Umbriel", "Titania", "Oberon"),
                planets.get(6).moons().stream().map(Moon::name).toList()
        );
        assertEquals(List.of("Triton"), planets.get(7).moons().stream().map(Moon::name).toList());
    }

    @Test
    void everyMoonHasUsablePhysicalAndDisplayValues() {
        for (Planet planet : PlanetDataLoader.loadFromClasspath("/planets.json")) {
            for (Moon moon : planet.moons()) {
                assertTrue(moon.periodDays() > 0, moon.name() + " needs a positive period");
                assertTrue(
                        moon.eccentricity() >= 0 && moon.eccentricity() < 1,
                        moon.name() + " needs an elliptical eccentricity"
                );
                assertTrue(moon.orbitRadiusFactor() > 0, moon.name() + " needs a positive orbit radius factor");
                assertTrue(moon.sizeFactor() > 0, moon.name() + " needs a positive size factor");
                assertTrue(moon.distanceKm() > 0, moon.name() + " needs a positive distance");
            }
        }
    }

    @Test
    void moonOrbitFactorsIncreaseWithRealDistance() {
        for (Planet planet : PlanetDataLoader.loadFromClasspath("/planets.json")) {
            List<Moon> moons = planet.moons();
            for (int i = 1; i < moons.size(); i++) {
                Moon inner = moons.get(i - 1);
                Moon outer = moons.get(i);
                assertTrue(
                        outer.distanceKm() > inner.distanceKm(),
                        planet.name() + ": " + outer.name() + " should be listed outside " + inner.name()
                );
                assertTrue(
                        outer.orbitRadiusFactor() > inner.orbitRadiusFactor(),
                        planet.name() + ": " + outer.name() + " should be drawn outside " + inner.name()
                );
            }
        }
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
