package com.raph.solarsystem.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolarSystemModelTest {
    @Test
    void initializesWithEightPlanetsInExpectedOrder() {
        SolarSystemModel model = new SolarSystemModel();

        assertEquals(8, model.planets().size());
        assertEquals("Mercury", model.planets().get(0).name());
        assertEquals("Neptune", model.planets().get(7).name());
    }

    @Test
    void planetListIsUnmodifiable() {
        SolarSystemModel model = new SolarSystemModel();

        assertThrows(UnsupportedOperationException.class, () -> model.planets().add(null));
    }

    @Test
    void advanceUpdatesTimeOnlyWhenNotPaused() {
        SolarSystemModel model = new SolarSystemModel();

        model.advance(2.0, 15.0, false);
        assertEquals(30.0, model.simDays(), 1e-12);

        model.advance(10.0, 99.0, true);
        assertEquals(30.0, model.simDays(), 1e-12);
    }

    @Test
    void resetSetsSimulationDaysToZero() {
        SolarSystemModel model = new SolarSystemModel();
        model.advance(1.0, 10.0, false);

        model.reset();
        assertEquals(0.0, model.simDays(), 1e-12);
    }

    @Test
    void maxAphelionMatchesNeptuneApproximateAphelion() {
        SolarSystemModel model = new SolarSystemModel();
        double expected = 30.07 * (1.0 + 0.0086);

        assertEquals(expected, model.maxAphelionAu(), 1e-10);
    }
}
