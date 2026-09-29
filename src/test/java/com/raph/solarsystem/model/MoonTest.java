package com.raph.solarsystem.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoonTest {
    @Test
    void unitPositionStartsAtPerihelionDistance() {
        Moon moon = new Moon("Moon", 0.0549, 27.322, 5.145, Color.GRAY, 3.0, 1.6, 384400);

        OrbitalPosition start = moon.unitPositionAtDays(0.0);
        double radius = Math.hypot(start.x(), start.y());

        assertEquals(1.0 - 0.0549, radius, 1e-10);
    }

    @Test
    void unitPositionRepeatsAfterOnePeriod() {
        Moon moon = new Moon("Moon", 0.0549, 27.322, 5.145, Color.GRAY, 3.0, 1.6, 384400);

        OrbitalPosition start = moon.unitPositionAtDays(0.0);
        OrbitalPosition afterPeriod = moon.unitPositionAtDays(27.322);

        assertEquals(start.x(), afterPeriod.x(), 1e-9);
        assertEquals(start.y(), afterPeriod.y(), 1e-9);
    }
}
