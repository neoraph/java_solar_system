package com.raph.solarsystem.controller;

import com.raph.solarsystem.model.SolarSystemModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolarSystemControllerTest {
    @Test
    void togglePauseSwitchesState() {
        SolarSystemController controller = new SolarSystemController(new SolarSystemModel());

        assertFalse(controller.paused());
        assertTrue(controller.togglePause());
        assertFalse(controller.togglePause());
    }

    @Test
    void advanceUsesControllerSpeedAndPauseState() {
        SolarSystemController controller = new SolarSystemController(new SolarSystemModel());
        controller.setDaysPerSecond(20.0);

        controller.advance(1.5);
        assertEquals(30.0, controller.model().simDays(), 1e-12);

        controller.togglePause();
        controller.advance(5.0);
        assertEquals(30.0, controller.model().simDays(), 1e-12);
    }

    @Test
    void tiltStrengthIsClampedBetweenZeroAndOne() {
        SolarSystemController controller = new SolarSystemController(new SolarSystemModel());

        controller.setTiltStrength(-4.0);
        assertEquals(0.0, controller.tiltStrength(), 1e-12);

        controller.setTiltStrength(8.0);
        assertEquals(1.0, controller.tiltStrength(), 1e-12);
    }

    @Test
    void labelsVisibilityCanBeToggled() {
        SolarSystemController controller = new SolarSystemController(new SolarSystemModel());

        assertTrue(controller.labelsVisible());
        controller.setLabelsVisible(false);
        assertFalse(controller.labelsVisible());
        controller.setLabelsVisible(true);
        assertTrue(controller.labelsVisible());
    }

    @Test
    void moonsVisibilityDefaultsToTrueAndCanBeToggled() {
        SolarSystemController controller = new SolarSystemController(new SolarSystemModel());

        assertTrue(controller.moonsVisible());
        controller.setMoonsVisible(false);
        assertFalse(controller.moonsVisible());
        controller.setMoonsVisible(true);
        assertTrue(controller.moonsVisible());
    }

    @Test
    void moonRendersArraySizedToTotalMoonCount() {
        SolarSystemController controller = new SolarSystemController(new SolarSystemModel());
        int expectedMoonCount = controller.model().planets().stream()
                .mapToInt(p -> p.moons().size())
                .sum();

        assertEquals(expectedMoonCount, controller.moonRenders().length);
    }
}
