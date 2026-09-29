package com.raph.solarsystem.view;

import java.util.Locale;

public record RenderContext(
        double centerX,
        double centerY,
        double scale,
        int width,
        int height,
        boolean paused,
        boolean hoverInfoEnabled,
        boolean labelsVisible,
        boolean moonsVisible,
        Locale locale,
        PlanetRender hoverRender,
        PlanetRender[] planetRenders,
        MoonRender[] moonRenders
) {
    public void updatePlanetRender(int index, PlanetRender render) {
        planetRenders[index] = render;
    }

    public void updateMoonRender(int index, MoonRender render) {
        moonRenders[index] = render;
    }
}
