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

