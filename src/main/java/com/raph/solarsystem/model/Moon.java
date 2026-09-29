package com.raph.solarsystem.model;

import java.awt.Color;

/**
 * A moon orbiting a {@link Planet}.
 *
 * <p>{@code eccentricity}, {@code periodDays} and {@code inclinationDeg} are real
 * astronomical values, so motion speed and orbit shape stay physically accurate.
 * {@code orbitRadiusFactor} and {@code sizeFactor} are display-only: real moon
 * distances (a few planet-radii) would be invisible at the AU-to-pixel scale used
 * for planets, so moons are drawn at an artistic distance expressed as a multiple
 * of their parent planet's rendered radius (consistent with planets already being
 * drawn at artistic, not-to-scale sizes).</p>
 *
 * <p>Because of that, a moon's semi-major axis is a unit circle rather than a real
 * distance; {@code distanceKm} keeps the true value for display in tooltips.</p>
 */
public class Moon extends OrbitingBody {
    private final double orbitRadiusFactor;
    private final double sizeFactor;
    private final double distanceKm;

    public Moon(
            String name,
            double eccentricity,
            double periodDays,
            double inclinationDeg,
            Color color,
            double orbitRadiusFactor,
            double sizeFactor,
            double distanceKm
    ) {
        super(name, eccentricity, periodDays, inclinationDeg, color);
        this.orbitRadiusFactor = orbitRadiusFactor;
        this.sizeFactor = sizeFactor;
        this.distanceKm = distanceKm;
    }

    public double orbitRadiusFactor() {
        return orbitRadiusFactor;
    }

    public double sizeFactor() {
        return sizeFactor;
    }

    public double distanceKm() {
        return distanceKm;
    }

    @Override
    public double semiMajorAxis() {
        return 1.0;
    }

    /** Position on a unit circle (semi-major axis = 1); scale by the desired pixel distance. */
    public OrbitalPosition unitPositionAtDays(double days) {
        return positionAtDays(days);
    }
}
