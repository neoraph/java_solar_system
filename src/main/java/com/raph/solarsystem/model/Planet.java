package com.raph.solarsystem.model;

import java.awt.Color;

public class Planet {
    private final String name;
    private final double semiMajorAu;
    private final double eccentricity;
    private final double periodDays;
    private final double inclinationDeg;
    private final Color color;

    public Planet(String name, double semiMajorAu, double eccentricity, double periodDays, double inclinationDeg, Color color) {
        this.name = name;
        this.semiMajorAu = semiMajorAu;
        this.eccentricity = eccentricity;
        this.periodDays = periodDays;
        this.inclinationDeg = inclinationDeg;
        this.color = color;
    }

    public String name() {
        return name;
    }

    public double semiMajorAu() {
        return semiMajorAu;
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

    public OrbitalPosition positionAtDays(double days) {
        double meanMotion = 2.0 * Math.PI / periodDays;
        double meanAnomaly = (meanMotion * days) % (2.0 * Math.PI);
        double eccAnomaly = solveKepler(meanAnomaly, eccentricity);

        double cosE = Math.cos(eccAnomaly);
        double sinE = Math.sin(eccAnomaly);
        double r = semiMajorAu * (1.0 - eccentricity * cosE);

        double trueAnomaly = Math.atan2(Math.sqrt(1 - eccentricity * eccentricity) * sinE, cosE - eccentricity);

        double x = r * Math.cos(trueAnomaly);
        double y = r * Math.sin(trueAnomaly);
        return new OrbitalPosition(x, y);
    }

    private static double solveKepler(double M, double e) {
        double E = M;
        for (int i = 0; i < 8; i++) {
            double f = E - e * Math.sin(E) - M;
            double fp = 1 - e * Math.cos(E);
            E = E - f / fp;
        }
        return E;
    }
}
