package com.raph.solarsystem.view;

import com.raph.solarsystem.i18n.I18n;
import com.raph.solarsystem.model.Moon;
import com.raph.solarsystem.model.OrbitalPosition;
import com.raph.solarsystem.model.Planet;
import com.raph.solarsystem.model.SolarSystemModel;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolarSystemRenderer {
    private boolean tiltEnabled = true;
    private double tiltStrength = 0.6;
    private double bodyScale = 1.0;
    private boolean antialiasEnabled = true;
    private int orbitSegments = 360;
    private final List<Path2D> orbitCache = new ArrayList<>();
    private final Map<String, String> labelCache = new HashMap<>();
    private double cachedCenterX;
    private double cachedCenterY;
    private double cachedScale;
    private int cachedPlanetCount = -1;
    private boolean orbitCacheValid = false;

    public void render(Graphics2D g2, SolarSystemModel model, RenderContext ctx) {
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                antialiasEnabled ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF
        );
        drawSun(g2, model, ctx);
        drawOrbits(g2, model, ctx);
        drawPlanets(g2, model, ctx);
        drawOverlay(g2, model, ctx);
        drawHoverPanel(g2, ctx);
    }

    public void setTiltEnabled(boolean enabled) {
        if (tiltEnabled != enabled) {
            tiltEnabled = enabled;
            orbitCacheValid = false;
        }
    }

    public void setTiltStrength(double value) {
        double clamped = clamp(value, 0.0, 1.0);
        if (Double.compare(tiltStrength, clamped) != 0) {
            tiltStrength = clamped;
            orbitCacheValid = false;
        }
    }

    public void setAntialiasEnabled(boolean enabled) {
        antialiasEnabled = enabled;
    }

    /**
     * Grows planets and moons as the view zooms in. Without this, zooming only spreads
     * orbits apart while bodies stay a few pixels wide, so deep zoom reveals no detail.
     * The exponent keeps growth sub-linear so bodies never swamp the screen.
     */
    public void setZoom(double zoom) {
        bodyScale = clamp(Math.pow(Math.max(zoom, 0.01), 0.55), 0.8, 9.0);
    }

    public void setOrbitSegments(int segments) {
        int clamped = (int) clamp(segments, 72, 360);
        if (orbitSegments != clamped) {
            orbitSegments = clamped;
            orbitCacheValid = false;
        }
    }

    private void drawSun(Graphics2D g2, SolarSystemModel model, RenderContext ctx) {
        double cx = ctx.centerX();
        double cy = ctx.centerY();
        double minPerihelionAu = model.planets().stream()
                .mapToDouble(p -> p.semiMajorAu() * (1.0 - p.eccentricity()))
                .min()
                .orElse(0.3);
        double minPerihelionPx = minPerihelionAu * ctx.scale();
        double r = clamp(minPerihelionPx * 0.35, 1.2, 18.0);

        g2.setColor(ThemeColors.SUN);
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g2.setColor(ThemeColors.SUN_LABEL);
        g2.drawString(I18n.tr(ctx.locale(), "renderer.sun"), (int) (cx + r + 6), (int) (cy - r - 2));
    }

    private void drawOrbits(Graphics2D g2, SolarSystemModel model, RenderContext ctx) {
        g2.setColor(ThemeColors.ORBIT);
        if (!orbitCacheValid || isOrbitCacheStale(model, ctx)) {
            rebuildOrbitCache(model, ctx);
        }
        for (Path2D orbit : orbitCache) {
            g2.draw(orbit);
        }
    }

    private void drawPlanets(Graphics2D g2, SolarSystemModel model, RenderContext ctx) {
        List<Planet> planets = model.planets();
        List<PlanetDraw> draws = new ArrayList<>(planets.size());
        List<LabeledBody> labeledBodies = new ArrayList<>();
        int[] moonOffsets = new int[planets.size()];
        int runningMoonOffset = 0;
        for (int i = 0; i < planets.size(); i++) {
            moonOffsets[i] = runningMoonOffset;
            runningMoonOffset += planets.get(i).moons().size();
        }
        for (int i = 0; i < planets.size(); i++) {
            Planet p = planets.get(i);
            OrbitalPosition pos = p.positionAtDays(model.simDays());
            ProjectedPoint proj = project(pos.x(), pos.y(), p.inclinationDeg());
            double x = ctx.centerX() + proj.x() * ctx.scale();
            double y = ctx.centerY() + proj.y() * ctx.scale();
            double radius = planetRadius(p.name()) * bodyScale * proj.sizeScale() * perspectiveSizeScale(proj.depth());
            draws.add(new PlanetDraw(i, p, pos, x, y, radius, proj.depth()));
        }

        // Draw far planets first so close ones overlap naturally in 3D mode.
        draws.sort(Comparator.comparingDouble(PlanetDraw::depth).reversed());
        for (PlanetDraw draw : draws) {
            g2.setColor(applyDepth(draw.planet().color(), draw.depth()));
            g2.fill(new Ellipse2D.Double(
                    draw.screenX() - draw.radius(),
                    draw.screenY() - draw.radius(),
                    draw.radius() * 2,
                    draw.radius() * 2
            ));

            if (ctx.moonsVisible()) {
                drawMoons(g2, model, ctx, draw, moonOffsets[draw.index()], labeledBodies);
            }

            labeledBodies.add(new LabeledBody(
                    draw.index(),
                    false,
                    draw.screenX(),
                    draw.screenY(),
                    draw.radius(),
                    planetLabel(ctx, draw.planet())
            ));

            ctx.updatePlanetRender(
                    draw.index(),
                    new PlanetRender(draw.planet(), draw.screenX(), draw.screenY(), draw.radius(), draw.modelPos().x(), draw.modelPos().y())
            );
        }

        if (ctx.labelsVisible()) {
            drawBodyLabels(g2, labeledBodies, ctx);
        }
    }

    private void drawMoons(
            Graphics2D g2,
            SolarSystemModel model,
            RenderContext ctx,
            PlanetDraw parentDraw,
            int moonBaseIndex,
            List<LabeledBody> labeledBodies
    ) {
        List<Moon> moons = parentDraw.planet().moons();
        Color faintOrbit = new Color(ThemeColors.ORBIT.getRed(), ThemeColors.ORBIT.getGreen(), ThemeColors.ORBIT.getBlue(), 90);
        for (int j = 0; j < moons.size(); j++) {
            Moon moon = moons.get(j);
            double orbitRadiusPx = parentDraw.radius() * moon.orbitRadiusFactor();

            g2.setColor(faintOrbit);
            g2.draw(new Ellipse2D.Double(
                    parentDraw.screenX() - orbitRadiusPx,
                    parentDraw.screenY() - orbitRadiusPx,
                    orbitRadiusPx * 2,
                    orbitRadiusPx * 2
            ));

            OrbitalPosition unitPos = moon.unitPositionAtDays(model.simDays());
            ProjectedPoint proj = project(unitPos.x() * orbitRadiusPx, unitPos.y() * orbitRadiusPx, moon.inclinationDeg());
            double mx = parentDraw.screenX() + proj.x();
            double my = parentDraw.screenY() + proj.y();
            double mr = moon.sizeFactor() * bodyScale * proj.sizeScale();

            g2.setColor(applyDepth(moon.color(), parentDraw.depth()));
            g2.fill(new Ellipse2D.Double(mx - mr, my - mr, mr * 2, mr * 2));

            labeledBodies.add(new LabeledBody(moonBaseIndex + j, true, mx, my, mr, moonLabel(ctx, moon)));

            ctx.updateMoonRender(moonBaseIndex + j, new MoonRender(parentDraw.planet(), moon, mx, my, mr));
        }
    }

    private String planetLabel(RenderContext ctx, Planet planet) {
        return labelCache.computeIfAbsent(
                "planet|" + planet.name() + "|" + ctx.locale().getLanguage(),
                ignored -> I18n.tr(
                        ctx.locale(),
                        "renderer.planetLabel",
                        I18n.planetName(ctx.locale(), planet.name()),
                        String.format(ctx.locale(), "%.1f", planet.semiMajorAu()),
                        String.format(ctx.locale(), "%.0f", planet.periodDays())
                )
        );
    }

    private String moonLabel(RenderContext ctx, Moon moon) {
        return labelCache.computeIfAbsent(
                "moon|" + moon.name() + "|" + ctx.locale().getLanguage(),
                ignored -> I18n.tr(
                        ctx.locale(),
                        "renderer.moonLabel",
                        I18n.moonName(ctx.locale(), moon.name()),
                        String.format(ctx.locale(), "%.1f", moon.periodDays())
                )
        );
    }

    /**
     * Places and draws labels for planets and moons through a single collision-avoiding
     * pass, so a moon label never overlaps a planet label (or another moon label).
     * Planets are laid out first to keep their preferred anchor positions.
     */
    private void drawBodyLabels(Graphics2D g2, List<LabeledBody> bodies, RenderContext ctx) {
        Font baseFont = g2.getFont();
        Font moonFont = baseFont.deriveFont(Math.max(9.0f, baseFont.getSize2D() - 2.0f));
        FontMetrics planetFm = g2.getFontMetrics(baseFont);
        FontMetrics moonFm = g2.getFontMetrics(moonFont);

        List<LabeledBody> ordered = new ArrayList<>(bodies);
        ordered.sort(Comparator.comparing(LabeledBody::moon).thenComparingInt(LabeledBody::order));

        List<Rectangle> occupiedLabels = new ArrayList<>(ordered.size());
        for (LabeledBody body : ordered) {
            FontMetrics fm = body.moon() ? moonFm : planetFm;
            String label = body.label();
            Point labelAnchor = placeLabel(body, label, fm, occupiedLabels, ctx.width(), ctx.height());
            drawLeaderLine(g2, body, labelAnchor, fm.stringWidth(label), fm);
            g2.setFont(body.moon() ? moonFont : baseFont);
            g2.setColor(body.moon() ? ThemeColors.MOON_LABEL : ThemeColors.LABEL);
            g2.drawString(label, labelAnchor.x, labelAnchor.y);
            occupiedLabels.add(new Rectangle(
                    labelAnchor.x - 2,
                    labelAnchor.y - fm.getAscent(),
                    fm.stringWidth(label) + 4,
                    fm.getHeight()
            ));
        }
        g2.setFont(baseFont);
    }

    private void drawOverlay(Graphics2D g2, SolarSystemModel model, RenderContext ctx) {
        g2.setColor(ThemeColors.OVERLAY_TEXT);
        double years = model.simDays() / 365.25;
        String pausedSuffix = ctx.paused() ? I18n.tr(ctx.locale(), "renderer.pausedSuffix") : "";
        String text = I18n.tr(
                ctx.locale(),
                "renderer.simTime",
                String.format(ctx.locale(), "%.1f", model.simDays()),
                String.format(ctx.locale(), "%.2f", years),
                pausedSuffix
        );
        g2.drawString(text, 12, ctx.height() - 40);
    }

    private void drawHoverPanel(Graphics2D g2, RenderContext ctx) {
        if (!ctx.hoverInfoEnabled() || ctx.hoverRender() == null) {
            return;
        }
        Planet p = ctx.hoverRender().planet();
        String line1 = I18n.planetName(ctx.locale(), p.name());
        String line2 = I18n.tr(
                ctx.locale(),
                "renderer.hoverLine2",
                String.format(ctx.locale(), "%.3f", p.semiMajorAu()),
                String.format(ctx.locale(), "%.3f", p.eccentricity()),
                String.format(ctx.locale(), "%.2f", p.inclinationDeg())
        );
        String line3 = I18n.tr(
                ctx.locale(),
                "renderer.hoverLine3",
                String.format(ctx.locale(), "%.0f", p.periodDays()),
                String.format(ctx.locale(), "%.2f", ctx.hoverRender().modelX()),
                String.format(ctx.locale(), "%.2f", ctx.hoverRender().modelY())
        );

        FontMetrics fm = g2.getFontMetrics();
        int padding = 10;
        int width = Math.max(fm.stringWidth(line1), Math.max(fm.stringWidth(line2), fm.stringWidth(line3))) + padding * 2;
        int height = fm.getHeight() * 3 + padding * 2;
        int x = ctx.width() - width - 12;
        int y = 12;

        g2.setColor(ThemeColors.HUD_BG);
        g2.fillRoundRect(x, y, width, height, 12, 12);
        g2.setColor(ThemeColors.HUD_TEXT);
        g2.drawRoundRect(x, y, width, height, 12, 12);

        int textY = y + padding + fm.getAscent();
        g2.drawString(line1, x + padding, textY);
        textY += fm.getHeight();
        g2.drawString(line2, x + padding, textY);
        textY += fm.getHeight();
        g2.drawString(line3, x + padding, textY);
    }

    private ProjectedPoint project(double x, double y, double inclinationDeg) {
        if (!tiltEnabled) {
            return new ProjectedPoint(x, y, 0.0, 1.0);
        }
        // Exaggerate orbital inclination so 3D differences are easier to perceive.
        double inc = Math.toRadians(inclinationDeg) * (1.0 + 10.0 * tiltStrength);
        double yi = y * Math.cos(inc);
        double zi = y * Math.sin(inc);

        // Apply a camera tilt around X, then a light perspective projection.
        double cameraTilt = Math.toRadians(60.0) * tiltStrength;
        double yCam = yi * Math.cos(cameraTilt) - zi * Math.sin(cameraTilt);
        double zCam = yi * Math.sin(cameraTilt) + zi * Math.cos(cameraTilt);
        double perspective = 1.0 / clamp(1.0 + zCam * 0.03, 0.55, 1.9);

        double xProj = x * perspective;
        double yProj = yCam * perspective;
        double sizeScale = clamp(0.72 + 0.55 * perspective, 0.7, 1.45);
        return new ProjectedPoint(xProj, yProj, zCam, sizeScale);
    }

    private Color applyDepth(Color base, double depth) {
        double factor = clamp(1.0 - depth * (0.05 + 0.08 * tiltStrength), 0.55, 1.0);
        int r = (int) clamp(base.getRed() * factor, 0, 255);
        int g = (int) clamp(base.getGreen() * factor, 0, 255);
        int b = (int) clamp(base.getBlue() * factor, 0, 255);
        return new Color(r, g, b);
    }

    private double planetRadius(String name) {
        return switch (name) {
            case "Jupiter" -> 8.0;
            case "Saturn" -> 7.0;
            case "Uranus" -> 6.0;
            case "Neptune" -> 6.0;
            default -> 4.0;
        };
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double perspectiveSizeScale(double depth) {
        if (!tiltEnabled) {
            return 1.0;
        }
        return clamp(1.0 / (1.0 + depth * 0.02), 0.75, 1.25);
    }

    private Point placeLabel(
            LabeledBody body,
            String label,
            FontMetrics fm,
            List<Rectangle> occupiedLabels,
            int width,
            int height
    ) {
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getHeight();
        int ascent = fm.getAscent();
        int margin = 6;
        int baseX = (int) (body.screenX() + body.radius() + margin);
        int baseY = (int) (body.screenY() - body.radius() - 3);

        int[] xOffsets = new int[]{0, 0, -textWidth - (int) (body.radius() * 2) - 12, -textWidth - (int) (body.radius() * 2) - 12};
        int[] yOffsets = new int[]{0, textHeight + 2, 0, textHeight + 2};
        Point best = null;
        double bestScore = Double.MAX_VALUE;
        for (int ring = 0; ring < 10; ring++) {
            for (int i = 0; i < xOffsets.length; i++) {
                int x = baseX + xOffsets[i];
                int y = baseY + yOffsets[i] + ring * textHeight;
                x = Math.max(4, Math.min(width - textWidth - 4, x));
                y = Math.max(ascent + 4, Math.min(height - 4, y));
                Rectangle box = new Rectangle(x - 2, y - ascent, textWidth + 4, textHeight);
                if (intersectsAny(box, occupiedLabels)) {
                    continue;
                }
                double score = Math.hypot(x - baseX, y - baseY);
                if (score < bestScore) {
                    bestScore = score;
                    best = new Point(x, y);
                }
            }
        }
        if (best != null) {
            return best;
        }
        return new Point(Math.max(4, Math.min(width - textWidth - 4, baseX)), Math.max(ascent + 4, Math.min(height - 4, baseY)));
    }

    private boolean intersectsAny(Rectangle box, List<Rectangle> occupiedLabels) {
        for (Rectangle occupied : occupiedLabels) {
            if (occupied.intersects(box)) {
                return true;
            }
        }
        return false;
    }

    private void drawLeaderLine(Graphics2D g2, LabeledBody body, Point labelAnchor, int textWidth, FontMetrics fm) {
        double labelCenterY = labelAnchor.y - fm.getAscent() / 2.0;
        double labelCenterX = labelAnchor.x + textWidth / 2.0;
        double dx = labelCenterX - body.screenX();
        double dy = labelCenterY - body.screenY();
        double dist = Math.hypot(dx, dy);
        if (dist < body.radius() + 8.0) {
            return;
        }

        double ux = dx / dist;
        double uy = dy / dist;
        int startX = (int) Math.round(body.screenX() + ux * (body.radius() + 2.0));
        int startY = (int) Math.round(body.screenY() + uy * (body.radius() + 2.0));
        int endX = (int) Math.round(labelAnchor.x + (labelCenterX >= body.screenX() ? -3.0 : textWidth + 3.0));
        int endY = (int) Math.round(labelCenterY);

        Stroke prevStroke = g2.getStroke();
        Color prevColor = g2.getColor();
        g2.setColor(ThemeColors.LEADER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawLine(startX, startY, endX, endY);
        g2.setStroke(prevStroke);
        g2.setColor(prevColor);
    }

    private boolean isOrbitCacheStale(SolarSystemModel model, RenderContext ctx) {
        return Double.compare(cachedCenterX, ctx.centerX()) != 0
                || Double.compare(cachedCenterY, ctx.centerY()) != 0
                || Double.compare(cachedScale, ctx.scale()) != 0
                || cachedPlanetCount != model.planets().size();
    }

    private void rebuildOrbitCache(SolarSystemModel model, RenderContext ctx) {
        orbitCache.clear();
        for (Planet p : model.planets()) {
            Path2D orbit = new Path2D.Double();
            boolean first = true;
            for (int i = 0; i <= orbitSegments; i++) {
                double theta = (2.0 * Math.PI * i) / orbitSegments;
                double r = p.semiMajorAu() * (1 - p.eccentricity() * p.eccentricity())
                        / (1 + p.eccentricity() * Math.cos(theta));
                double x = r * Math.cos(theta);
                double y = r * Math.sin(theta);
                ProjectedPoint proj = project(x, y, p.inclinationDeg());
                double sx = ctx.centerX() + proj.x() * ctx.scale();
                double sy = ctx.centerY() + proj.y() * ctx.scale();
                if (first) {
                    orbit.moveTo(sx, sy);
                    first = false;
                } else {
                    orbit.lineTo(sx, sy);
                }
            }
            orbitCache.add(orbit);
        }
        cachedCenterX = ctx.centerX();
        cachedCenterY = ctx.centerY();
        cachedScale = ctx.scale();
        cachedPlanetCount = model.planets().size();
        orbitCacheValid = true;
    }

    private record ProjectedPoint(double x, double y, double depth, double sizeScale) {}

    private record PlanetDraw(
            int index,
            Planet planet,
            OrbitalPosition modelPos,
            double screenX,
            double screenY,
            double radius,
            double depth
    ) {}

    /** A planet or a moon reduced to what the shared label pass needs. */
    private record LabeledBody(
            int order,
            boolean moon,
            double screenX,
            double screenY,
            double radius,
            String label
    ) {}
}
