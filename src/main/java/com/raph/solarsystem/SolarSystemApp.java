package com.raph.solarsystem;

import com.raph.solarsystem.controller.SolarSystemController;
import com.raph.solarsystem.i18n.I18n;
import com.raph.solarsystem.model.Planet;
import com.raph.solarsystem.model.SolarSystemModel;
import com.raph.solarsystem.view.ControlsPanel;
import com.raph.solarsystem.view.PlanetRender;
import com.raph.solarsystem.view.RenderContext;
import com.raph.solarsystem.view.SolarSystemRenderer;
import com.raph.solarsystem.view.ThemeColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class SolarSystemApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(I18n.tr(java.util.Locale.getDefault(), "app.title"));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 800);
            frame.setLocationRelativeTo(null);
            SolarPanel solarPanel = new SolarPanel();
            frame.setContentPane(buildRootPanel(solarPanel, locale -> frame.setTitle(I18n.tr(locale, "app.title"))));
            frame.setVisible(true);
        });
    }

    private static JComponent buildRootPanel(SolarPanel solarPanel, Consumer<java.util.Locale> onLocaleChanged) {
        JPanel root = new JPanel(new BorderLayout());
        root.add(solarPanel, BorderLayout.CENTER);
        root.add(new ControlsPanel(
                solarPanel.controller(),
                solarPanel::repaint,
                solarPanel::setTargetFps,
                solarPanel::setPerformanceMode,
                solarPanel::setZoom,
                onLocaleChanged
        ), BorderLayout.SOUTH);
        return root;
    }

    static class SolarPanel extends JPanel {
        private static final int DEFAULT_FPS = 30;
        private static final double MIN_ZOOM = 0.25;
        private static final double MAX_ZOOM = 4.0;
        private final SolarSystemController controller;
        private final SolarSystemRenderer renderer;
        private final List<Point> stars = new java.util.ArrayList<>();
        private final Timer timer;
        private long lastNanos = System.nanoTime();
        private BufferedImage starField;
        private int starFieldWidth = -1;
        private int starFieldHeight = -1;
        private double zoom = 1.0;

        SolarPanel() {
            setBackground(ThemeColors.SPACE_BG);
            setDoubleBuffered(true);

            controller = new SolarSystemController(new SolarSystemModel());
            renderer = new SolarSystemRenderer();

            initStars(220);

            setToolTipText("");
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    controller.setHoverRender(findHover(e.getX(), e.getY()));
                    repaint();
                }
            });
            addMouseWheelListener(e -> {
                double factor = e.getPreciseWheelRotation() < 0 ? 1.1 : (1.0 / 1.1);
                setZoom(zoom * factor);
                repaint();
            });
            setFocusable(true);
            setupKeyboardShortcuts();

            timer = new Timer(1000 / DEFAULT_FPS, this::tick);
            timer.start();
        }

        private void setupKeyboardShortcuts() {
            InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getActionMap();

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "zoomIn");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "zoomIn");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "zoomIn");

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoomOut");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "zoomOut");

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0), "zoomFit");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), "zoomFit");

            actionMap.put("zoomIn", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setZoom(zoom * 1.1);
                    repaint();
                }
            });
            actionMap.put("zoomOut", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setZoom(zoom / 1.1);
                    repaint();
                }
            });
            actionMap.put("zoomFit", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setZoom(1.0);
                    repaint();
                }
            });
        }

        private void initStars(int count) {
            Random rng = new Random(42);
            for (int i = 0; i < count; i++) {
                int x = rng.nextInt(2000) - 500;
                int y = rng.nextInt(1400) - 300;
                stars.add(new Point(x, y));
            }
        }

        private void tick(ActionEvent e) {
            long now = System.nanoTime();
            double dt = (now - lastNanos) / 1_000_000_000.0;
            lastNanos = now;
            controller.advance(dt);
            if (!controller.paused()) {
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();
            double cx = w / 2.0;
            double cy = h / 2.0;

            paintStars(g2, w, h);

            double scale = (0.45 * Math.min(w, h) / controller.model().maxAphelionAu()) * zoom;

            renderer.setTiltEnabled(controller.tiltEnabled());
            renderer.setTiltStrength(controller.tiltStrength());
            renderer.render(g2, controller.model(), new RenderContext(
                    cx,
                    cy,
                    scale,
                    w,
                    h,
                    controller.paused(),
                    controller.hoverInfoEnabled(),
                    controller.labelsVisible(),
                    controller.locale(),
                    controller.hoverRender(),
                    controller.planetRenders()
            ));

            g2.dispose();
        }

        private void paintStars(Graphics2D g2, int w, int h) {
            if (starField == null || starFieldWidth != w || starFieldHeight != h) {
                starField = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                starFieldWidth = w;
                starFieldHeight = h;
                Graphics2D sg = starField.createGraphics();
                sg.setColor(ThemeColors.STAR);
                for (Point p : stars) {
                    int x = (p.x + w / 2) % w;
                    int y = (p.y + h / 2) % h;
                    if (x < 0) x += w;
                    if (y < 0) y += h;
                    sg.fillRect(x, y, 2, 2);
                }
                sg.dispose();
            }
            g2.drawImage(starField, 0, 0, null);
        }

        SolarSystemController controller() {
            return controller;
        }

        @Override
        public String getToolTipText(java.awt.event.MouseEvent event) {
            if (!controller.hoverInfoEnabled() || controller.planetRenders() == null) {
                return null;
            }
            PlanetRender best = findHover(event.getX(), event.getY());
            if (best == null) {
                return null;
            }
            Planet p = best.planet();
            return I18n.tr(
                    controller.locale(),
                    "tooltip.planet",
                    I18n.planetName(controller.locale(), p.name()),
                    String.format(controller.locale(), "%.3f", p.semiMajorAu()),
                    String.format(controller.locale(), "%.3f", p.eccentricity()),
                    String.format(controller.locale(), "%.2f", p.inclinationDeg()),
                    String.format(controller.locale(), "%.0f", p.periodDays())
            );
        }

        private PlanetRender findHover(double mx, double my) {
            PlanetRender best = null;
            double bestDist = Double.MAX_VALUE;
            for (PlanetRender render : controller.planetRenders()) {
                if (render == null) {
                    continue;
                }
                double dx = mx - render.screenX();
                double dy = my - render.screenY();
                double dist = Math.hypot(dx, dy);
                if (dist < render.radius() + 10 && dist < bestDist) {
                    bestDist = dist;
                    best = render;
                }
            }
            return best;
        }

        void setTargetFps(int fps) {
            int safeFps = Math.max(1, fps);
            timer.setDelay(1000 / safeFps);
            timer.setInitialDelay(1000 / safeFps);
        }

        void setPerformanceMode(boolean enabled) {
            renderer.setAntialiasEnabled(!enabled);
            renderer.setOrbitSegments(enabled ? 120 : 360);
        }

        void setZoom(double zoomValue) {
            zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomValue));
        }
    }

    // Renderer context and render items live in view package.
}
