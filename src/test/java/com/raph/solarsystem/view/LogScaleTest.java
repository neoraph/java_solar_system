package com.raph.solarsystem.view;

import com.raph.solarsystem.view.ControlsPanel.LogScale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogScaleTest {
    private static final LogScale SCALE = new LogScale(0.01, 400.0, 1000);

    @Test
    void sliderEndsMapToTheConfiguredRange() {
        assertEquals(0.01, SCALE.toValue(0), 1e-9);
        assertEquals(400.0, SCALE.toValue(1000), 1e-9);
    }

    @Test
    void valuesRoundTripWithinSliderResolution() {
        for (double value : new double[]{0.01, 0.05, 1.0, 15.0, 100.0, 400.0}) {
            double roundTripped = SCALE.toValue(SCALE.toSlider(value));
            assertEquals(value, roundTripped, value * 0.01, "round trip failed for " + value);
        }
    }

    @Test
    void valuesOutsideTheRangeAreClamped() {
        assertEquals(0, SCALE.toSlider(-5.0));
        assertEquals(1000, SCALE.toSlider(10_000.0));
    }

    @Test
    void mappingIsMonotonicAndGeometric() {
        assertTrue(SCALE.toValue(100) < SCALE.toValue(200));
        // Equal slider steps must multiply the value by a constant factor.
        double lowRatio = SCALE.toValue(400) / SCALE.toValue(200);
        double highRatio = SCALE.toValue(800) / SCALE.toValue(600);
        assertEquals(lowRatio, highRatio, 1e-9);
    }

    @Test
    void speedScaleReachesSpeedsSlowEnoughToReadFastMoonLabels() {
        // Phobos orbits in 0.3189 days; it needs several seconds per orbit to be readable.
        double slowest = ControlsPanel.SPEED_SCALE.toValue(0);
        assertTrue(0.3189 / slowest > 5.0, "slowest speed should stretch a Phobos orbit past 5 seconds");
    }

    @Test
    void zoomScaleSpansTheDocumentedRange() {
        assertEquals(0.25, ControlsPanel.ZOOM_SCALE.toValue(0), 1e-9);
        assertEquals(200.0, ControlsPanel.ZOOM_SCALE.toValue(ControlsPanel.ZOOM_SCALE.steps()), 1e-9);
    }
}
