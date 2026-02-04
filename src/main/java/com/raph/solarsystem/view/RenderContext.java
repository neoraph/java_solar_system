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
        Locale locale,
        PlanetRender hoverRender,
        PlanetRender[] planetRenders
) {
    public void updatePlanetRender(int index, PlanetRender render) {
        planetRenders[index] = render;
    }
}
