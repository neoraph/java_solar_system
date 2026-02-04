# Physics Model

This app uses a simplified two-body Keplerian model per planet (Sun + one planet), then renders all planets together.

## Orbital Parameters Used

- `a` (`semiMajorAu`): semi-major axis, in AU.
- `e` (`eccentricity`): ellipse eccentricity.
- `P` (`periodDays`): orbital period, in days.
- `i` (`inclinationDeg`): used only for visual projection in this app.

Useful derived values:

- Perihelion distance: `q = a(1 - e)`
- Aphelion distance: `Q = a(1 + e)`

## Time-To-Position Equations

At simulation time `t` (days):

1. Mean motion:
- `n = 2π / P`

2. Mean anomaly:
- `M = (n * t) mod 2π`

3. Solve Kepler equation for eccentric anomaly `E`:
- `M = E - e sin(E)`

4. Radius from focus (Sun):
- `r = a(1 - e cos(E))`

5. True anomaly `ν`:
- `ν = atan2(sqrt(1-e^2) sin(E), cos(E)-e)`

6. Orbital-plane position:
- `x = r cos(ν)`
- `y = r sin(ν)`

## Why It Looks Faster Near Perihelion

Kepler's second law (equal areas in equal times) implies orbital speed is higher near perihelion and lower near aphelion. This behavior naturally appears when propagating with `M -> E -> ν`.

## What This Simulation Intentionally Simplifies

- No N-body interactions between planets.
- No perturbations, resonances, or precession.
- Orbital elements are constant, loaded from JSON.
- Inclination and camera tilt in rendering are visual choices, not full 3D dynamics.

## External References

- Kepler's laws: https://en.wikipedia.org/wiki/Kepler%27s_laws_of_planetary_motion
- Kepler's equation: https://en.wikipedia.org/wiki/Kepler%27s_equation
- Mean anomaly: https://en.wikipedia.org/wiki/Mean_anomaly
- Eccentric anomaly: https://en.wikipedia.org/wiki/Eccentric_anomaly
- Semi-major axis overview: https://en.wikipedia.org/wiki/Semi-major_and_semi-minor_axes

JPL/NASA glossaries:

- Semi-major axis: https://ssd.jpl.nasa.gov/glossary/semimajor_axis.html
- Eccentricity: https://ssd.jpl.nasa.gov/glossary/eccentricity.html
- Mean anomaly: https://ssd.jpl.nasa.gov/glossary/ma.html
- Mean motion: https://ssd.jpl.nasa.gov/glossary/n.html
- Keplerian elements context: https://science.nasa.gov/learn/basics-of-space-flight/chapter5-1/
