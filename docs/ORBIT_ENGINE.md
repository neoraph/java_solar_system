# Orbit Engine (Code Map)

This document maps orbital formulas to the current Java implementation.

## Data Source

- Planet constants are loaded from `src/main/resources/planets.json`.
- Loader: `src/main/java/com/raph/solarsystem/model/PlanetDataLoader.java`.

Each planet includes:

- `semiMajorAu`
- `eccentricity`
- `periodDays`
- `inclinationDeg` (visual projection)

## Simulation Clock

- Global simulation time is stored in `SolarSystemModel.simDays`.
- Time update: `simDays += dtSeconds * daysPerSecond`.
- File: `src/main/java/com/raph/solarsystem/model/SolarSystemModel.java`.

## Planet Position Computation

Method:

- `Planet.positionAtDays(double days)`
- File: `src/main/java/com/raph/solarsystem/model/Planet.java`.

Pipeline in that method:

1. `meanMotion = 2π / periodDays`
2. `meanAnomaly = (meanMotion * days) % 2π`
3. `eccAnomaly = solveKepler(meanAnomaly, eccentricity)`
4. `r = semiMajorAu * (1 - eccentricity * cos(E))`
5. `trueAnomaly = atan2(sqrt(1-e^2) sin(E), cos(E)-e)`
6. `x = r cos(trueAnomaly)`, `y = r sin(trueAnomaly)`

Return type:

- `OrbitalPosition(x, y)`.

## Kepler Equation Solver

- Method: `Planet.solveKepler(double M, double e)`.
- Uses Newton-Raphson:
  - `f(E) = E - e sin(E) - M`
  - `f'(E) = 1 - e cos(E)`
  - `E <- E - f/f'`
- Current implementation runs a fixed 8 iterations.

Notes:

- `8` is an implementation choice, not a physical constant.
- It is accurate enough for this dataset and frame-rate usage.

## Orbit Scale for Rendering

- Method: `SolarSystemModel.maxAphelionAu()`.
- Formula per planet: `a(1 + e)` (aphelion distance).
- Renderer uses the max aphelion to fit all orbits on screen.

## Rendering Layer

- 2D orbit/planet rendering + optional 3D-like visual tilt:
  - `src/main/java/com/raph/solarsystem/view/SolarSystemRenderer.java`
- The tilt/inclination transforms are visual projection only and do not change orbital dynamics.
