package com.raph.solarsystem.view;

import com.raph.solarsystem.i18n.I18n;
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
    private static final Color ORBIT_COLOR = new Color(80, 90, 120);
    private static final Color LABEL_COLOR = new Color(220, 220, 230);
    private static final Color LABEL_LEADER_COLOR = new Color(170, 182, 210, 180);
    private static final Color HUD_BG = new Color(12, 16, 28, 210);

    private boolean tiltEnabled = true;
    private double tiltStrength = 0.6;
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
        drawSun(g2, ctx.centerX(), ctx.centerY(), ctx.locale());
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

    public void setOrbitSegments(int segments) {
        int clamped = (int) clamp(segments, 72, 360);
        if (orbitSegments != clamped) {
            orbitSegments = clamped;
            orbitCacheValid = false;
        }
    }

    private void drawSun(Graphics2D g2, double cx, double cy, java.util.Locale locale) {
        g2.setColor(new Color(255, 210, 60));
        double r = 18.0;
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g2.setColor(new Color(255, 230, 140));
        g2.drawString(I18n.tr(locale, "renderer.sun"), (int) (cx + 22), (int) (cy - 10));
    }

    private void drawOrbits(Graphics2D g2, SolarSystemModel model, RenderContext ctx) {
        g2.setColor(ORBIT_COLOR);
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
        List<LabeledPlanetDraw> labeledDraws = new ArrayList<>(planets.size());
        for (int i = 0; i < planets.size(); i++) {
            Planet p = planets.get(i);
            OrbitalPosition pos = p.positionAtDays(model.simDays());
            ProjectedPoint proj = project(pos.x(), pos.y(), p.inclinationDeg());
            double x = ctx.centerX() + proj.x() * ctx.scale();
            double y = ctx.centerY() + proj.y() * ctx.scale();
            double radius = planetRadius(p.name()) * proj.sizeScale() * perspectiveSizeScale(proj.depth());
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

            g2.setColor(LABEL_COLOR);
            String cacheKey = draw.planet().name() + "|" + ctx.locale().getLanguage();
            String label = labelCache.computeIfAbsent(
                    cacheKey,
                    ignored -> I18n.tr(
                            ctx.locale(),
                            "renderer.planetLabel",
                            I18n.planetName(ctx.locale(), draw.planet().name()),
                            String.format(ctx.locale(), "%.1f", draw.planet().semiMajorAu()),
                            String.format(ctx.locale(), "%.0f", draw.planet().periodDays())
                    )
            );
            labeledDraws.add(new LabeledPlanetDraw(draw, label));

            ctx.updatePlanetRender(
                    draw.index(),
                    new PlanetRender(draw.planet(), draw.screenX(), draw.screenY(), draw.radius(), draw.modelPos().x(), draw.modelPos().y())
            );
        }

        if (ctx.labelsVisible()) {
            FontMetrics fm = g2.getFontMetrics();
            List<Rectangle> occupiedLabels = new ArrayList<>(labeledDraws.size());
            labeledDraws.sort(Comparator.comparingInt(item -> item.draw().index()));
            for (LabeledPlanetDraw item : labeledDraws) {
                PlanetDraw draw = item.draw();
                String label = item.label();
                Point labelAnchor = placeLabel(draw, label, fm, occupiedLabels, ctx.width(), ctx.height());
                drawLeaderLine(g2, draw, labelAnchor, fm.stringWidth(label), fm);
                g2.setColor(LABEL_COLOR);
                g2.drawString(label, labelAnchor.x, labelAnchor.y);
                occupiedLabels.add(new Rectangle(
                        labelAnchor.x - 2,
                        labelAnchor.y - fm.getAscent(),
                        fm.stringWidth(label) + 4,
                        fm.getHeight()
                ));
            }
        }
    }

    private void drawOverlay(Graphics2D g2, SolarSystemModel model, RenderContext ctx) {
        g2.setColor(new Color(200, 210, 230));
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

        g2.setColor(HUD_BG);
        g2.fillRoundRect(x, y, width, height, 12, 12);
        g2.setColor(new Color(220, 230, 245));
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
            PlanetDraw draw,
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
        int baseX = (int) (draw.screenX() + draw.radius() + margin);
        int baseY = (int) (draw.screenY() - draw.radius() - 3);

        int[] xOffsets = new int[]{0, 0, -textWidth - (int) (draw.radius() * 2) - 12, -textWidth - (int) (draw.radius() * 2) - 12};
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

    private void drawLeaderLine(Graphics2D g2, PlanetDraw draw, Point labelAnchor, int textWidth, FontMetrics fm) {
        double labelCenterY = labelAnchor.y - fm.getAscent() / 2.0;
        double labelCenterX = labelAnchor.x + textWidth / 2.0;
        double dx = labelCenterX - draw.screenX();
        double dy = labelCenterY - draw.screenY();
        double dist = Math.hypot(dx, dy);
        if (dist < draw.radius() + 8.0) {
            return;
        }

        double ux = dx / dist;
        double uy = dy / dist;
        int startX = (int) Math.round(draw.screenX() + ux * (draw.radius() + 2.0));
        int startY = (int) Math.round(draw.screenY() + uy * (draw.radius() + 2.0));
        int endX = (int) Math.round(labelAnchor.x + (labelCenterX >= draw.screenX() ? -3.0 : textWidth + 3.0));
        int endY = (int) Math.round(labelCenterY);

        Stroke prevStroke = g2.getStroke();
        Color prevColor = g2.getColor();
        g2.setColor(LABEL_LEADER_COLOR);
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

    private record LabeledPlanetDraw(PlanetDraw draw, String label) {}
}
