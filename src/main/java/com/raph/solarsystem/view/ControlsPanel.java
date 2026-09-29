package com.raph.solarsystem.view;

import com.raph.solarsystem.controller.SolarSystemController;
import com.raph.solarsystem.i18n.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class ControlsPanel extends JPanel {
    /** Logarithmic so the useful low end doesn't collapse into a few pixels. */
    static final LogScale ZOOM_SCALE = new LogScale(0.25, 200.0, 1000);

    /** Fast moons (Phobos orbits in 0.32 d) need sub-day/sec speeds to be readable. */
    static final LogScale SPEED_SCALE = new LogScale(0.01, 400.0, 1000);

    private final SolarSystemController controller;
    private final Runnable onStateChanged;
    private final IntConsumer onFpsChanged;
    private final Consumer<Boolean> onPerformanceModeChanged;
    private final Consumer<Double> onZoomChanged;
    private final Runnable onResetView;
    private final Consumer<Locale> onLocaleChanged;

    private final JLabel speedLabel;
    private final JButton pauseButton;
    private final JButton resetButton;
    private final JCheckBox tiltBox;
    private final JLabel tiltLabel;
    private final JCheckBox labelsBox;
    private final JCheckBox moonsBox;
    private final JCheckBox hoverBox;
    private final JLabel fpsLabel;
    private final JCheckBox perfModeBox;
    private final JLabel zoomLabel;
    private final JButton zoomFit;
    private final JLabel languageLabel;
    private final JComboBox<LocaleOption> languageBox;
    private Locale lastAppliedLocale;

    public ControlsPanel(
            SolarSystemController controller,
            Runnable onStateChanged,
            IntConsumer onFpsChanged,
            Consumer<Boolean> onPerformanceModeChanged,
            Consumer<Double> onZoomChanged,
            Runnable onResetView,
            Consumer<Locale> onLocaleChanged
    ) {
        this.controller = controller;
        this.onStateChanged = onStateChanged;
        this.onFpsChanged = onFpsChanged;
        this.onPerformanceModeChanged = onPerformanceModeChanged;
        this.onZoomChanged = onZoomChanged;
        this.onResetView = onResetView;
        this.onLocaleChanged = onLocaleChanged;

        setBackground(ThemeColors.PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        speedLabel = createLabel();
        JSlider speedSlider = createLogSlider(SPEED_SCALE, 15.0, value -> {
            speedLabel.setText(tr("controls.speed", formatSpeed(value)));
            controller.setDaysPerSecond(value);
            onStateChanged.run();
        });
        JButton presetSlow = createButton("0.05x", () -> setSpeedPreset(speedSlider, 0.05));
        JButton preset1 = createButton("1x", () -> setSpeedPreset(speedSlider, 1));
        JButton preset10 = createButton("10x", () -> setSpeedPreset(speedSlider, 10));
        JButton preset100 = createButton("100x", () -> setSpeedPreset(speedSlider, 100));

        pauseButton = createButton(null, button -> {
            boolean paused = controller.togglePause();
            button.setText(tr(paused ? "controls.resume" : "controls.pause"));
        });
        resetButton = createButton(null, controller::resetTime);

        tiltBox = createCheckBox(controller.tiltEnabled(), controller::setTiltEnabled);
        tiltLabel = createLabel();
        JSlider tiltSlider = createSlider(0, 100, 60, value -> {
            tiltLabel.setText(tr("controls.tiltStrength", value));
            controller.setTiltStrength(value / 100.0);
            onStateChanged.run();
        });

        hoverBox = createCheckBox(true, controller::setHoverInfoEnabled);
        labelsBox = createCheckBox(controller.labelsVisible(), controller::setLabelsVisible);
        moonsBox = createCheckBox(controller.moonsVisible(), controller::setMoonsVisible);

        fpsLabel = createLabel();
        JSlider fpsSlider = createSlider(10, 120, 30, fps -> {
            fpsLabel.setText(tr("controls.fps", fps));
            onFpsChanged.accept(fps);
        });
        JButton fps20 = createButton("20 FPS", () -> fpsSlider.setValue(20));
        JButton fps30 = createButton("30 FPS", () -> fpsSlider.setValue(30));
        JButton fps60 = createButton("60 FPS", () -> fpsSlider.setValue(60));

        perfModeBox = createCheckBox(false, onPerformanceModeChanged::accept);

        zoomLabel = createLabel();
        JSlider zoomSlider = createLogSlider(ZOOM_SCALE, 1.0, zoom -> {
            zoomLabel.setText(tr("controls.zoom", formatZoomPercent(zoom)));
            onZoomChanged.accept(zoom);
            onStateChanged.run();
        });
        zoomFit = createButton(null, () -> {
            zoomSlider.setValue(ZOOM_SCALE.toSlider(1.0));
            onResetView.run();
        });

        languageLabel = createLabel();
        languageBox = createLanguageBox(speedSlider, tiltSlider, fpsSlider, zoomSlider);

        layOutControls(
                speedSlider, presetSlow, preset1, preset10, preset100,
                tiltSlider, fpsSlider, fps20, fps30, fps60, zoomSlider
        );

        refreshTexts(speedSlider.getValue(), tiltSlider.getValue(), fpsSlider.getValue(), zoomSlider.getValue());
    }

    private JLabel createLabel() {
        JLabel label = new JLabel();
        label.setForeground(ThemeColors.CONTROL_FG);
        return label;
    }

    /** Creates a checkbox that pushes its state to {@code onToggle} and then refreshes the view. */
    private JCheckBox createCheckBox(boolean selected, Consumer<Boolean> onToggle) {
        JCheckBox box = new JCheckBox();
        box.setForeground(ThemeColors.CONTROL_FG);
        box.setOpaque(false);
        box.setSelected(selected);
        box.addActionListener(e -> {
            onToggle.accept(box.isSelected());
            onStateChanged.run();
        });
        return box;
    }

    /** Creates a button; pass {@code null} as text for buttons whose caption is localized later. */
    private JButton createButton(String text, Runnable action) {
        return createButton(text, button -> action.run());
    }

    /** Variant for actions that need to mutate the button itself (e.g. a toggling caption). */
    private JButton createButton(String text, Consumer<JButton> action) {
        JButton button = text == null ? new JButton() : new JButton(text);
        button.addActionListener(e -> {
            action.accept(button);
            onStateChanged.run();
        });
        return button;
    }

    private JSlider createSlider(int min, int max, int value, IntConsumer onChange) {
        JSlider slider = new JSlider(min, max, value);
        slider.setOpaque(false);
        slider.addChangeListener(e -> onChange.accept(slider.getValue()));
        return slider;
    }

    /** Wraps {@link #createSlider} so callers work in real units instead of raw slider ticks. */
    private JSlider createLogSlider(LogScale scale, double initialValue, DoubleConsumer onChange) {
        return createSlider(0, scale.steps(), scale.toSlider(initialValue), tick -> onChange.accept(scale.toValue(tick)));
    }

    /** Moves the slider, then re-applies the exact value so slider quantization can't drift it. */
    private void setSpeedPreset(JSlider slider, double daysPerSecond) {
        slider.setValue(SPEED_SCALE.toSlider(daysPerSecond));
        controller.setDaysPerSecond(daysPerSecond);
    }

    private JComboBox<LocaleOption> createLanguageBox(
            JSlider speedSlider,
            JSlider tiltSlider,
            JSlider fpsSlider,
            JSlider zoomSlider
    ) {
        JComboBox<LocaleOption> box = new JComboBox<>(new LocaleOption[]{
                new LocaleOption(I18n.EN),
                new LocaleOption(I18n.FR),
                new LocaleOption(I18n.JA)
        });
        lastAppliedLocale = controller.locale();
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LocaleOption option) {
                    label.setText(tr(localeLabelKey(option.locale())));
                }
                return label;
            }
        });
        box.setSelectedIndex(Math.max(0, I18n.supportedLocales().indexOf(controller.locale())));
        box.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            applySelectedLocale(speedSlider, tiltSlider, fpsSlider, zoomSlider);
        });
        box.addActionListener(e -> applySelectedLocale(speedSlider, tiltSlider, fpsSlider, zoomSlider));
        return box;
    }

    private void layOutControls(
            JSlider speedSlider,
            JButton presetSlow,
            JButton preset1,
            JButton preset10,
            JButton preset100,
            JSlider tiltSlider,
            JSlider fpsSlider,
            JButton fps20,
            JButton fps30,
            JButton fps60,
            JSlider zoomSlider
    ) {
        addSpaced(10, speedLabel, speedSlider);
        addGap(8);
        addSpaced(4, presetSlow, preset1, preset10, preset100);
        addGap(14);
        addSpaced(8, pauseButton, resetButton);
        addGap(14);
        addSpaced(8, tiltBox, tiltLabel);
        addGap(6);
        add(tiltSlider);
        addGap(10);
        addSpaced(10, hoverBox, labelsBox, moonsBox);
        addGap(10);
        addSpaced(6, fpsLabel, fpsSlider, fps20);
        addGap(4);
        addSpaced(4, fps30, fps60);
        addGap(10);
        add(perfModeBox);
        addGap(10);
        addSpaced(6, zoomLabel, zoomSlider, zoomFit);
        addGap(10);
        addSpaced(6, languageLabel, languageBox);
    }

    /** Adds components left to right, separated by {@code gap} pixels. */
    private void addSpaced(int gap, Component... components) {
        for (int i = 0; i < components.length; i++) {
            if (i > 0) {
                addGap(gap);
            }
            add(components[i]);
        }
    }

    private void addGap(int gap) {
        add(Box.createHorizontalStrut(gap));
    }

    private void refreshTexts(int speedSliderValue, int tilt, int fps, int zoomSliderValue) {
        speedLabel.setText(tr("controls.speed", formatSpeed(SPEED_SCALE.toValue(speedSliderValue))));
        pauseButton.setText(tr(controller.paused() ? "controls.resume" : "controls.pause"));
        resetButton.setText(tr("controls.reset"));
        tiltBox.setText(tr("controls.tilt"));
        tiltLabel.setText(tr("controls.tiltStrength", tilt));
        hoverBox.setText(tr("controls.hoverInfo"));
        labelsBox.setText(tr("controls.labels"));
        moonsBox.setText(tr("controls.moons"));
        fpsLabel.setText(tr("controls.fps", fps));
        perfModeBox.setText(tr("controls.performance"));
        zoomLabel.setText(tr("controls.zoom", formatZoomPercent(ZOOM_SCALE.toValue(zoomSliderValue))));
        zoomFit.setText(tr("controls.fit"));
        languageLabel.setText(tr("controls.language"));
    }

    private String tr(String key, Object... args) {
        return I18n.tr(controller.locale(), key, args);
    }

    private String localeLabelKey(Locale locale) {
        String language = I18n.normalize(locale).getLanguage();
        if (I18n.FR.getLanguage().equals(language)) {
            return "language.french";
        }
        if (I18n.JA.getLanguage().equals(language)) {
            return "language.japanese";
        }
        return "language.english";
    }

    private void applySelectedLocale(JSlider speedSlider, JSlider tiltSlider, JSlider fpsSlider, JSlider zoomSlider) {
        Object selectedItem = languageBox.getSelectedItem();
        if (!(selectedItem instanceof LocaleOption selected)) {
            return;
        }
        Locale normalized = I18n.normalize(selected.locale());
        if (Objects.equals(lastAppliedLocale, normalized)) {
            return;
        }
        lastAppliedLocale = normalized;
        controller.setLocale(normalized);
        Locale activeLocale = controller.locale();
        onLocaleChanged.accept(activeLocale);
        refreshTexts(speedSlider.getValue(), tiltSlider.getValue(), fpsSlider.getValue(), zoomSlider.getValue());
        languageBox.repaint();
        revalidate();
        repaint();
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.repaint();
        }
        onStateChanged.run();
    }

    private String formatZoomPercent(double zoom) {
        double percent = zoom * 100.0;
        return String.format(controller.locale(), percent < 1000 ? "%.0f" : "%,.0f", percent);
    }

    private String formatSpeed(double daysPerSecond) {
        String pattern = daysPerSecond < 0.1 ? "%.3f" : daysPerSecond < 10 ? "%.2f" : "%.0f";
        return String.format(controller.locale(), pattern, daysPerSecond);
    }

    /** Maps a linear slider range onto an exponential value range. */
    record LogScale(double min, double max, int steps) {
        int toSlider(double value) {
            double clamped = Math.max(min, Math.min(max, value));
            return (int) Math.round(Math.log(clamped / min) / Math.log(max / min) * steps);
        }

        double toValue(int sliderValue) {
            return min * Math.pow(max / min, (double) sliderValue / steps);
        }
    }

    private record LocaleOption(Locale locale) {}
}
