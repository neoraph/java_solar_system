# Planet Data

Planet parameters are loaded from:

- `src/main/resources/planets.json`

Each planet entry uses:

- `name` - display name
- `semiMajorAu` - semi-major axis in AU
- `eccentricity` - orbital eccentricity (`0..1`)
- `periodDays` - orbital period in days
- `inclinationDeg` - inclination used for 3D-like projection
- `color` - display color as `#RRGGBB`
- `moons` - optional array of moon entries (omit or leave empty for none)

Example:

```json
{
  "name": "Earth",
  "semiMajorAu": 1.0,
  "eccentricity": 0.0167,
  "periodDays": 365.25,
  "inclinationDeg": 0.0,
  "color": "#5082FF",
  "moons": [
    {
      "name": "Moon",
      "eccentricity": 0.0549,
      "periodDays": 27.322,
      "inclinationDeg": 5.145,
      "color": "#C8C8C8",
      "orbitRadiusFactor": 3.0,
      "sizeFactor": 1.6,
      "distanceKm": 384400
    }
  ]
}
```

## Moons

Each moon entry uses:

- `name` - display name
- `eccentricity`, `periodDays`, `inclinationDeg` - real orbital values, used to drive
  motion speed and orbit shape accurately (same Kepler-equation math as planets,
  shared via `KeplerOrbit`)
- `color` - display color as `#RRGGBB`
- `orbitRadiusFactor` - **display-only** distance, expressed as a multiple of the
  parent planet's *rendered* pixel radius. Real moon distances (a few planet radii)
  would be invisible at the AU-to-pixel scale used for planets, so this is an
  artistic distance, consistent with planets already being drawn at
  not-to-scale sizes (see `SolarSystemRenderer.planetRadius`).
- `sizeFactor` - display-only dot radius in pixels
- `distanceKm` - real average orbital distance in km, shown in the hover tooltip
  for reference (not used for rendering)

## Notes

- Colors are validated when loading.
- Loading is performed by `PlanetDataLoader`.
- The model reads data from classpath resource `/planets.json`.
- Moons are optional; a planet without a `moons` array gets an empty list.

### What We Can Infer

The values should not be treated as an authoritative ephemeris dataset.

Reference material used for orbital parameter ranges and terminology:

- NASA Planetary Fact Sheet (planet constants): https://nssdc.gsfc.nasa.gov/planetary/factsheet/
- NASA Space Place (eccentricity/intuitive orbit explanations): https://spaceplace.nasa.gov/elliptical-orbits/en/
- JPL Solar System Dynamics glossary:
  - Semi-major axis: https://ssd.jpl.nasa.gov/glossary/semimajor_axis.html
  - Eccentricity: https://ssd.jpl.nasa.gov/glossary/eccentricity.html
  - Mean anomaly: https://ssd.jpl.nasa.gov/glossary/ma.html
  - Mean motion: https://ssd.jpl.nasa.gov/glossary/n.html
- Wikipedia (concept references):
  - Orbital elements: https://en.wikipedia.org/wiki/Orbital_elements
  - Kepler's equation: https://en.wikipedia.org/wiki/Kepler%27s_equation

If you need reproducible "planet position on date D" behavior, document and store:

- the orbital element epoch (for example J2000),
- source for each orbital element,
- per-planet phase at epoch (for example mean anomaly at epoch).
