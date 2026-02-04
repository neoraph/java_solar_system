package com.raph.solarsystem.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanetTest {
    @Test
    void completesOneOrbitAtExpectedPeriod() {
        Planet earth = new Planet("Earth", 1.0, 0.0167, 365.25, 0.0, Color.BLUE);
        OrbitalPosition start = earth.positionAtDays(0.0);
        OrbitalPosition oneYear = earth.positionAtDays(365.25);

        assertEquals(start.x(), oneYear.x(), 1e-9);
        assertEquals(start.y(), oneYear.y(), 1e-9);
    }

    @Test
    void startsAtPerihelionDistanceWhenDayIsZero() {
        Planet earth = new Planet("Earth", 1.0, 0.0167, 365.25, 0.0, Color.BLUE);
        OrbitalPosition start = earth.positionAtDays(0.0);
        double radius = Math.hypot(start.x(), start.y());

        assertEquals(1.0 * (1.0 - 0.0167), radius, 1e-10);
    }

    @Test
    void positionRemainsFiniteForHighEccentricityOrbit() {
        Planet cometLike = new Planet("Test", 3.0, 0.8, 1000.0, 10.0, Color.WHITE);
        OrbitalPosition pos = cometLike.positionAtDays(123.45);

        assertTrue(Double.isFinite(pos.x()));
        assertTrue(Double.isFinite(pos.y()));
    }
}
