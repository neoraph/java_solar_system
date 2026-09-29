package com.raph.solarsystem.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrbitingBodyTest {
    private static final Planet EARTH =
            new Planet("Earth", 1.0, 0.0167, 365.25, 0.0, Color.BLUE);
    private static final Moon MOON =
            new Moon("Moon", 0.0549, 27.322, 5.145, Color.GRAY, 3.0, 1.6, 384400);

    @Test
    void planetsAndMoonsShareTheSameOrbitingBodyApi() {
        List<OrbitingBody> bodies = List.of(EARTH, MOON);

        for (OrbitingBody body : bodies) {
            assertTrue(body.periodDays() > 0, body.name() + " must expose a period");
            assertTrue(body.eccentricity() >= 0, body.name() + " must expose an eccentricity");
            assertEquals(body.name(), body.name());
        }
    }

    @Test
    void semiMajorAxisUsesEachSubtypesOwnUnit() {
        assertEquals(1.0, EARTH.semiMajorAxis(), 1e-12);
        assertEquals(EARTH.semiMajorAu(), EARTH.semiMajorAxis(), 1e-12);
        // A moon orbits a unit circle that the renderer scales to its parent's radius.
        assertEquals(1.0, MOON.semiMajorAxis(), 1e-12);
    }

    @Test
    void inheritedPositionMatchesDirectKeplerComputation() {
        OrbitalPosition expected = KeplerOrbit.positionAtDays(1.0, 0.0167, 365.25, 120.0);
        OrbitalPosition actual = EARTH.positionAtDays(120.0);

        assertEquals(expected.x(), actual.x(), 1e-12);
        assertEquals(expected.y(), actual.y(), 1e-12);
    }

    @Test
    void moonUnitPositionIsTheInheritedPosition() {
        OrbitalPosition viaAlias = MOON.unitPositionAtDays(10.0);
        OrbitalPosition viaBase = MOON.positionAtDays(10.0);

        assertEquals(viaBase.x(), viaAlias.x(), 1e-12);
        assertEquals(viaBase.y(), viaAlias.y(), 1e-12);
    }

    @Test
    void polymorphicPositionWorksWithoutKnowingTheSubtype() {
        for (OrbitingBody body : List.of(EARTH, MOON)) {
            OrbitalPosition start = body.positionAtDays(0.0);
            double radius = Math.hypot(start.x(), start.y());
            double expectedPerihelion = body.semiMajorAxis() * (1.0 - body.eccentricity());

            assertEquals(expectedPerihelion, radius, 1e-9, body.name() + " should start at perihelion");
        }
    }

    @Test
    void onlyPlanetsCarryMoons() {
        assertTrue(EARTH.moons().isEmpty());
        Planet withMoon = new Planet("Earth", 1.0, 0.0167, 365.25, 0.0, Color.BLUE, List.of(MOON));
        assertEquals(1, withMoon.moons().size());
        assertEquals("Moon", withMoon.moons().get(0).name());
    }
}
