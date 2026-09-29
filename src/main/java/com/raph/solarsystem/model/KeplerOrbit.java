package com.raph.solarsystem.model;

/**
 * Shared elliptical-orbit math (Kepler's equation), usable for any body
 * orbiting a focus: planets around the Sun, or moons around a planet.
 */
public final class KeplerOrbit {

    private KeplerOrbit() {}

    /**
     * meanMotion  is the mean motion of the orbit (which is the rate at which the orbiting body moves around the focus, expressed in radians per day).
     * meanAnomaly is the mean anomaly of the orbit, expressed in radians (which is the angle between the orbital object and the perihelion of the orbit).
     * eccAnomaly is the eccentric anomaly of the orbit, which is the angle between the orbital object and a point on the orbit.
     * trueAnomaly is the true anomaly of the orbit, which is the angle between the orbital
     *  and the position of the orbiting body.
     *
     * we have to calculate the cosine and sine of eccAnomaly to get the trueAnomaly.
     *
     * reference : https://en.wikipedia.org/wiki/Orbital_elements
     *
     * @param semiMajor
     * @param eccentricity
     * @param periodDays
     * @param days
     * @return
     */
    public static OrbitalPosition positionAtDays(double semiMajor, double eccentricity, double periodDays, double days) {
        double meanMotion = 2.0 * Math.PI / periodDays;
        double meanAnomaly = (meanMotion * days) % (2.0 * Math.PI);
        double eccAnomaly = solveKepler(meanAnomaly, eccentricity);

        double cosE = Math.cos(eccAnomaly);
        double sinE = Math.sin(eccAnomaly);
        double r = semiMajor * (1.0 - eccentricity * cosE);

        double trueAnomaly = Math.atan2(Math.sqrt(1 - eccentricity * eccentricity) * sinE, cosE - eccentricity);

        double x = r * Math.cos(trueAnomaly);
        double y = r * Math.sin(trueAnomaly);
        return new OrbitalPosition(x, y);
    }

    /**
     *  Kepler equation :
     *  E = M + e sin(E)
     *  M is the mean anomaly (mean the elapse of the orbit around the focus, expressed in radians proportional to the orbital period),
     *  E is the eccentric anomaly (the eccentric anomaly is the angle between the orbital plane and a point on the orbit),
     *  e is the eccentricity (the ratio of the orbital radius to the orbital semi-major axis, between 0 and 1).
     *
     *  Because E cannot usually be calculated directly, the code starts with an estimate of E and improves it iteratively using Newton's method.
     *  The method is based on the Newton-Raphson method, which is an iterative method for finding successively better approximations to the roots (or zeroes) of a real-valued function.
     *
     *  The Newton-Raphson method is a root-finding algorithm that produces successively better approximations to the roots (or zeroes) of a real-valued function.
     *  It starts with an initial guess and refines it by using the function's derivative to approximate the function's behavior near the root.
     *  The method is known for its fast convergence, but it requires the function to be differentiable and the initial guess to be sufficiently close to the root.
     *
     *  f = eccAnomaly - eccentricity * sin(eccAnomaly) - meanAnomaly represents the "error" of the current guess : how far the estimate is from satisfying Kepler's equation
     *  fPrime = 1 - eccentricity * cos(eccAnomaly) represents the derivative of the error function, which is the slope of the tangent line at the current guess
     *  correction = f / fPrime is the Newton adjustment: move E in the direction that reduces the error.
     *  eccAnomaly -= correction updates the estimate closer to the true solution.
     *
     *  Correction is the amount by which the estimate changed. If its absolute value is less than 1e-12, the iteration is considered to have converged,
     *  so we can stop iterating and return the final estimate.
     *
     *  In this case, we are using the Newton-Raphson method to solve Kepler's equation, which is a transcendental equation that relates the mean anomaly, eccentric anomaly, and eccentricity of an orbit.
     *  The method is particularly useful for solving Kepler's equation because it converges quickly and efficiently, even for highly eccentric orbits.
     *
     * @param meanAnomaly
     * @param eccentricity
     * @return
     */
    private static double solveKepler(double meanAnomaly, double eccentricity) {
        double eccAnomaly = meanAnomaly;

        for (int i = 0; i < 8; i++) {
            double f = eccAnomaly - eccentricity * Math.sin(eccAnomaly) - meanAnomaly;
            double fPrime = 1 - eccentricity * Math.cos(eccAnomaly);
            double correction = f / fPrime;
            eccAnomaly -= correction;

            if (Math.abs(correction) < 1e-12) {
                break;
            }
        }
        return eccAnomaly;
    }
}
