package com.raph.solarsystem.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PlanetDataLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PlanetDataLoader() {}

    public static List<Planet> loadFromClasspath(String resourcePath) {
        InputStream stream = PlanetDataLoader.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("Planet data not found: " + resourcePath);
        }
        try (InputStream input = stream) {
            return load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read planet data from " + resourcePath, e);
        }
    }

    static List<Planet> load(InputStream input) throws IOException {
        List<PlanetSpec> specs = MAPPER.readValue(input, new TypeReference<>() {});
        return specs.stream()
                .map(PlanetSpec::toPlanet)
                .toList();
    }

    private record PlanetSpec(
            String name,
            double semiMajorAu,
            double eccentricity,
            double periodDays,
            double inclinationDeg,
            String color,
            List<MoonSpec> moons
    ) {
        Planet toPlanet() {
            List<Moon> parsedMoons = moons == null
                    ? List.of()
                    : moons.stream().map(MoonSpec::toMoon).collect(Collectors.toList());
            return new Planet(name, semiMajorAu, eccentricity, periodDays, inclinationDeg, parseColor(color), parsedMoons);
        }
    }

    private record MoonSpec(
            String name,
            double eccentricity,
            double periodDays,
            double inclinationDeg,
            String color,
            double orbitRadiusFactor,
            double sizeFactor,
            double distanceKm
    ) {
        Moon toMoon() {
            return new Moon(
                    Objects.requireNonNull(name, "moon name"),
                    eccentricity,
                    periodDays,
                    inclinationDeg,
                    parseColor(color),
                    orbitRadiusFactor,
                    sizeFactor,
                    distanceKm
            );
        }
    }

    private static Color parseColor(String hex) {
        if (hex == null || !hex.matches("^#[0-9a-fA-F]{6}$")) {
            throw new IllegalArgumentException("Invalid color format, expected #RRGGBB: " + hex);
        }
        int rgb = Integer.parseInt(hex.substring(1), 16);
        return new Color(rgb);
    }
}
