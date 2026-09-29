package com.raph.solarsystem.i18n;

import com.raph.solarsystem.model.Moon;
import com.raph.solarsystem.model.Planet;
import com.raph.solarsystem.model.PlanetDataLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards against adding a body to planets.json without translating it: {@code moonName}
 * silently falls back to the English name, so a missing key is otherwise invisible.
 */
class BodyTranslationsTest {
    private static final List<Planet> PLANETS = PlanetDataLoader.loadFromClasspath("/planets.json");

    @Test
    void everyPlanetAndMoonHasAKeyInEveryLocale() {
        for (Locale locale : I18n.supportedLocales()) {
            ResourceBundle bundle = I18n.bundle(locale);
            for (Planet planet : PLANETS) {
                assertTrue(
                        bundle.containsKey("planet." + planet.name()),
                        "Missing planet." + planet.name() + " in " + locale
                );
                for (Moon moon : planet.moons()) {
                    assertTrue(
                            bundle.containsKey("moon." + moon.name()),
                            "Missing moon." + moon.name() + " in " + locale
                    );
                }
            }
        }
    }

    @Test
    void rendererLabelPatternsExistInEveryLocale() {
        for (Locale locale : I18n.supportedLocales()) {
            ResourceBundle bundle = I18n.bundle(locale);
            assertTrue(bundle.containsKey("renderer.planetLabel"), "Missing renderer.planetLabel in " + locale);
            assertTrue(bundle.containsKey("renderer.moonLabel"), "Missing renderer.moonLabel in " + locale);
        }
    }

    @Test
    void nonEnglishLocalesActuallyTranslateWellKnownBodies() {
        assertEquals("Japet", I18n.moonName(I18n.FR, "Iapetus"));
        assertEquals("\u30C8\u30EA\u30C8\u30F3", I18n.moonName(I18n.JA, "Triton"));
        assertEquals("Saturne", I18n.planetName(I18n.FR, "Saturn"));
    }

    @Test
    void unknownBodyFallsBackToItsEnglishName() {
        assertEquals("Nessus", I18n.moonName(I18n.FR, "Nessus"));
    }
}
