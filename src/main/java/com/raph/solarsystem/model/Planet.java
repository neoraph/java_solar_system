package com.raph.solarsystem.model;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

/**
 * A planet orbiting the Sun. Unlike a {@link Moon}, its semi-major axis is a real
 * distance in AU and it can carry moons of its own.
 */
public class Planet extends OrbitingBody {
    private final double semiMajorAu;
    private final List<Moon> moons;

    public Planet(String name, double semiMajorAu, double eccentricity, double periodDays, double inclinationDeg, Color color) {
        this(name, semiMajorAu, eccentricity, periodDays, inclinationDeg, color, List.of());
    }

    public Planet(
            String name,
            double semiMajorAu,
            double eccentricity,
            double periodDays,
            double inclinationDeg,
            Color color,
            List<Moon> moons
    ) {
        super(name, eccentricity, periodDays, inclinationDeg, color);
        this.semiMajorAu = semiMajorAu;
        this.moons = Collections.unmodifiableList(moons);
    }

    public double semiMajorAu() {
        return semiMajorAu;
    }

    @Override
    public double semiMajorAxis() {
        return semiMajorAu;
    }

    public List<Moon> moons() {
        return moons;
    }
}
