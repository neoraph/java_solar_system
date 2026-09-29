# Java Solar System (Swing)

Simple Java desktop simulation of the solar system with elliptical orbits, labels, controls, and a lightweight 3D-like projection.

## Tech Stack

- Java 25 by default (Gradle toolchain, override with `-PjavaVersion=21` if needed)
- Swing/AWT for UI and rendering
- Gradle (with wrapper)
- Jackson (JSON loading for planet data)
- JUnit 5 for unit tests

## Project Layout

- `src/main/java/com/raph/solarsystem/` - app entry point and UI wiring
- `src/main/java/com/raph/solarsystem/model/` - domain model + physics + JSON loader
- `src/main/java/com/raph/solarsystem/controller/` - simulation state/control logic
- `src/main/java/com/raph/solarsystem/view/` - renderer + controls panel
- `src/main/resources/planets.json` - planet dataset
- `src/test/java/com/raph/solarsystem/` - unit tests
- `docs/PLANET.md` - data documentation
- `docs/PHYSICS.md` - formulas and motion model
- `docs/ORBIT_ENGINE.md` - formula-to-code mapping

## Run

```bash
./gradlew run
```

If Java 25 is not installed locally, use:

```bash
./gradlew -PjavaVersion=21 run
```

## Test

```bash
./gradlew test
```

## Runtime Controls

- Speed slider (logarithmic, 0.01 to 400 days/sec) and presets (`0.05x`, `1x`, `10x`,
  `100x`); the slow end makes fast inner moons like Phobos readable
- Pause/Resume, Reset
- 3D tilt toggle + strength slider
- Hover info toggle
- Labels toggle - planet labels, plus moon labels (smaller font) when moons are
  shown; planets and moons share one collision-avoiding label layout pass
- Moons toggle (`Show moons`) - Earth's Moon, Mars' Phobos/Deimos, Jupiter's Galilean
  moons, and Saturn's Tethys/Dione/Rhea/Titan/Iapetus orbit their planets with real
  periods/eccentricities; hover a moon for its details. Only each planet's largest
  moons are represented (e.g. Jupiter and Saturn each have dozens more known moons,
  and Uranus/Neptune's moons aren't modeled at all), for clarity and performance.
- FPS slider + presets (`20`, `30`, `60`)
- Performance mode (reduced render cost)
- Zoom slider (logarithmic, 25% to 20000%) + `Fit` (resets zoom and recenters)
- Mouse wheel zoom, anchored on the cursor
- Drag with the mouse to pan the view
- Keyboard zoom: `+`, `-`, `0` (fit)
- Planets and moons grow as you zoom in, so deep zoom actually reveals detail
- Language selector: English / French / Japanese

## Quick Math Glossary

- `a` (`semiMajorAu`): semi-major axis, orbit size (in AU).
- `e` (`eccentricity`): orbit shape (`0` circular, closer to `1` more elongated).
- `P` (`periodDays`): time for one full orbit (days).
- `n` (mean motion): average angular speed, `n = 2π / P`.
- `M` (mean anomaly): time-based orbital phase angle.
- `E` (eccentric anomaly): intermediate angle solving Kepler equation.
- `ν` (true anomaly): actual geometric angle of the planet from perihelion.
- Perihelion: closest distance to Sun, `a(1 - e)`.
- Aphelion: farthest distance from Sun, `a(1 + e)`.

For details and formulas, see `docs/PHYSICS.md` and `docs/ORBIT_ENGINE.md`.

## Design Pattern (High-Level)

- Model-Controller-View split:
  - **Model**: planet data and orbital calculations
  - **Controller**: mutable simulation settings and time progression
  - **View**: rendering and UI controls
- `Planet` and `Moon` both extend the abstract `OrbitingBody`, which holds what they
  share (name, eccentricity, period, inclination, color) and the Kepler position
  computation. They stay separate types because they differ in what they orbit:
  `Planet` has a real semi-major axis in AU and owns a list of moons, while `Moon`
  orbits a unit circle that the renderer scales to its parent's drawn radius.

## Architecture Flow

```mermaid
flowchart LR
    A[ControlsPanel<br/>UI events] --> B[SolarSystemController<br/>state + simulation flags]
    B --> C[SolarSystemModel<br/>time + planets]
    C --> D[SolarSystemRenderer<br/>draw orbits/planets/HUD]
    D --> E[SolarPanel - Swing<br/>paintComponent]
    F[planets.json] --> C
```

Quick read:
- `ControlsPanel` updates controller settings (speed, fps, tilt, zoom, etc.)
- `SolarPanel` timer ticks call controller/model progression
- `SolarSystemRenderer` reads model + render context and draws the frame
- `planets.json` is the source of orbital/display data
