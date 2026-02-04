package com.raph.solarsystem.view;

import com.raph.solarsystem.model.Planet;

public record PlanetRender(Planet planet, double screenX, double screenY, double radius, double modelX, double modelY) {}
