package com.raph.solarsystem.controller;

import com.raph.solarsystem.i18n.I18n;
import com.raph.solarsystem.model.SolarSystemModel;
import com.raph.solarsystem.view.PlanetRender;

import java.util.Locale;

public class SolarSystemController {
    private final SolarSystemModel model;
    private final PlanetRender[] planetRenders;

    private double daysPerSecond = 15.0;
    private boolean paused = false;
    private boolean tiltEnabled = false;
    private double tiltStrength = 0.6;
    private boolean labelsVisible = true;
    private boolean hoverInfoEnabled = true;
    private PlanetRender hoverRender;
    private Locale locale = I18n.normalize(Locale.getDefault());

    public SolarSystemController(SolarSystemModel model) {
        this.model = model;
        this.planetRenders = new PlanetRender[model.planets().size()];
    }

    public SolarSystemModel model() {
        return model;
    }

    public PlanetRender[] planetRenders() {
        return planetRenders;
    }

    public PlanetRender hoverRender() {
        return hoverRender;
    }

    public void setHoverRender(PlanetRender render) {
        this.hoverRender = render;
    }

    public double daysPerSecond() {
        return daysPerSecond;
    }

    public void setDaysPerSecond(double value) {
        daysPerSecond = value;
    }

    public boolean paused() {
        return paused;
    }

    public boolean togglePause() {
        paused = !paused;
        return paused;
    }

    public void resetTime() {
        model.reset();
    }

    public boolean tiltEnabled() {
        return tiltEnabled;
    }

    public void setTiltEnabled(boolean enabled) {
        tiltEnabled = enabled;
    }

    public double tiltStrength() {
        return tiltStrength;
    }

    public void setTiltStrength(double value) {
        tiltStrength = clamp(value, 0.0, 1.0);
    }

    public boolean hoverInfoEnabled() {
        return hoverInfoEnabled;
    }

    public void setHoverInfoEnabled(boolean enabled) {
        hoverInfoEnabled = enabled;
    }

    public boolean labelsVisible() {
        return labelsVisible;
    }

    public void setLabelsVisible(boolean visible) {
        labelsVisible = visible;
    }

    public void advance(double dtSeconds) {
        model.advance(dtSeconds, daysPerSecond, paused);
    }

    public Locale locale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = I18n.normalize(locale);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
