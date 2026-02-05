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

Example:

```json
{
  "name": "Earth",
  "semiMajorAu": 1.0,
  "eccentricity": 0.0167,
  "periodDays": 365.25,
  "inclinationDeg": 0.0,
  "color": "#5082FF"
}
```

## Notes

- Colors are validated when loading.
- Loading is performed by `PlanetDataLoader`.
- The model reads data from classpath resource `/planets.json`.

## Sources

### Provenance Status (Current Repository)

- Exact source file or dataset for each numeric value is **not recorded** in the repository.
- `src/main/resources/planets.json` was introduced in the initial commit:
  - commit `a94559afbbf6738435a6e192895cd9ca6ef1cac1`
  - message: `Init Java Solar System with Swing`
- No citation or generation script is present in that commit.

### What We Can Infer

The values appear to be rounded, standard textbook/NASA-style orbital constants
for the 8 planets (good for visualization), but this is an inference.
They should not be treated as an authoritative ephemeris dataset.

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
