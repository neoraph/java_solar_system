package com.raph.solarsystem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SolarSystemModel {
    private final List<Planet> planets = new ArrayList<>();
    private double simDays = 0.0;

    public SolarSystemModel() {
        this(PlanetDataLoader.loadFromClasspath("/planets.json"));
    }

    SolarSystemModel(List<Planet> planets) {
        this.planets.addAll(planets);
    }

    public List<Planet> planets() {
        return Collections.unmodifiableList(planets);
    }

    public double simDays() {
        return simDays;
    }

    public void reset() {
        simDays = 0.0;
    }

    public void advance(double dtSeconds, double daysPerSecond, boolean paused) {
        if (paused) {
            return;
        }
        simDays += dtSeconds * daysPerSecond;
    }

    public double maxAphelionAu() {
        return planets.stream()
                .mapToDouble(p -> p.semiMajorAu() * (1.0 + p.eccentricity()))
                .max()
                .orElse(30.0);
    }
}
