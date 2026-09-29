package com.raph.solarsystem.view;

import com.raph.solarsystem.model.Moon;
import com.raph.solarsystem.model.Planet;

public record MoonRender(Planet parent, Moon moon, double screenX, double screenY, double radius) {}
