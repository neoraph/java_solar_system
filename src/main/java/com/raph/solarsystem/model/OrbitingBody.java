package com.raph.solarsystem.model;

import java.awt.Color;

/**
 * Common state and behaviour of anything orbiting a focus, whether that focus is
 * the Sun ({@link Planet}) or a planet ({@link Moon}).
 *
 * <p>Subclasses stay distinct because they differ in what they orbit and in how
 * their orbit is measured: a planet's semi-major axis is a real distance in AU,
 * while a moon's is a unit circle scaled at render time to a multiple of its
 * parent's drawn radius. That difference is expressed by {@link #semiMajorAxis()}.</p>
 */
public abstract class OrbitingBody {
    private final String name;
    private final double eccentricity;
    private final double periodDays;
    private final double inclinationDeg;
    private final Color color;

    protected OrbitingBody(
            String name,
            double eccentricity,
            double periodDays,
            double inclinationDeg,
            Color color
    ) {
        this.name = name;
        this.eccentricity = eccentricity;
        this.periodDays = periodDays;
        this.inclinationDeg = inclinationDeg;
        this.color = color;
    }

    public String name() {
        return name;
    }

    public double eccentricity() {
        return eccentricity;
    }

    public double periodDays() {
        return periodDays;
    }

    public double inclinationDeg() {
        return inclinationDeg;
    }

    public Color color() {
        return color;
    }

    /** Semi-major axis in this body's own unit: AU for planets, a unit circle for moons. */
    public abstract double semiMajorAxis();

    /** Position relative to the focus this body orbits, in the unit of {@link #semiMajorAxis()}. */
    public OrbitalPosition positionAtDays(double days) {
        return KeplerOrbit.positionAtDays(semiMajorAxis(), eccentricity, periodDays, days);
    }
}
