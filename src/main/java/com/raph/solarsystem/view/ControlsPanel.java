package com.raph.solarsystem.view;

import com.raph.solarsystem.controller.SolarSystemController;
import com.raph.solarsystem.i18n.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ControlsPanel extends JPanel {
    private final SolarSystemController controller;
    private final Runnable onStateChanged;
    private final IntConsumer onFpsChanged;
    private final Consumer<Boolean> onPerformanceModeChanged;
    private final Consumer<Double> onZoomChanged;
    private final Consumer<Locale> onLocaleChanged;

    private final JLabel speedLabel;
    private final JButton preset1;
    private final JButton preset10;
    private final JButton preset100;
    private final JButton pauseButton;
    private final JButton resetButton;
    private final JCheckBox tiltBox;
    private final JLabel tiltLabel;
    private final JCheckBox labelsBox;
    private final JCheckBox hoverBox;
    private final JLabel fpsLabel;
    private final JButton fps20;
    private final JButton fps30;
    private final JButton fps60;
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
            Consumer<Locale> onLocaleChanged
    ) {
        this.controller = controller;
        this.onStateChanged = onStateChanged;
        this.onFpsChanged = onFpsChanged;
        this.onPerformanceModeChanged = onPerformanceModeChanged;
        this.onZoomChanged = onZoomChanged;
        this.onLocaleChanged = onLocaleChanged;

        setBackground(ThemeColors.PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        speedLabel = new JLabel();
        speedLabel.setForeground(ThemeColors.CONTROL_FG);

        JSlider speedSlider = new JSlider(1, 400, 15);
        speedSlider.setOpaque(false);
        speedSlider.addChangeListener(e -> {
            int value = speedSlider.getValue();
            speedLabel.setText(I18n.tr(controller.locale(), "controls.speed", value));
            controller.setDaysPerSecond(value);
            onStateChanged.run();
        });

        preset1 = new JButton("1x");
        preset1.addActionListener(e -> setSpeedPreset(speedSlider, 1));
        preset10 = new JButton("10x");
        preset10.addActionListener(e -> setSpeedPreset(speedSlider, 10));
        preset100 = new JButton("100x");
        preset100.addActionListener(e -> setSpeedPreset(speedSlider, 100));

        pauseButton = new JButton();
        pauseButton.addActionListener(e -> {
            boolean paused = controller.togglePause();
            pauseButton.setText(I18n.tr(controller.locale(), paused ? "controls.resume" : "controls.pause"));
            onStateChanged.run();
        });

        resetButton = new JButton();
        resetButton.addActionListener(e -> {
            controller.resetTime();
            onStateChanged.run();
        });

        tiltBox = new JCheckBox();
        tiltBox.setForeground(ThemeColors.CONTROL_FG);
        tiltBox.setOpaque(false);
        tiltBox.setSelected(controller.tiltEnabled());
        tiltBox.addActionListener(e -> {
            controller.setTiltEnabled(tiltBox.isSelected());
            onStateChanged.run();
        });

        tiltLabel = new JLabel();
        tiltLabel.setForeground(ThemeColors.CONTROL_FG);
        JSlider tiltSlider = new JSlider(0, 100, 60);
        tiltSlider.setOpaque(false);
        tiltSlider.addChangeListener(e -> {
            int value = tiltSlider.getValue();
            tiltLabel.setText(I18n.tr(controller.locale(), "controls.tiltStrength", value));
            controller.setTiltStrength(value / 100.0);
            onStateChanged.run();
        });

        hoverBox = new JCheckBox();
        hoverBox.setForeground(ThemeColors.CONTROL_FG);
        hoverBox.setOpaque(false);
        hoverBox.setSelected(true);
        hoverBox.addActionListener(e -> {
            controller.setHoverInfoEnabled(hoverBox.isSelected());
            onStateChanged.run();
        });

        labelsBox = new JCheckBox();
        labelsBox.setForeground(ThemeColors.CONTROL_FG);
        labelsBox.setOpaque(false);
        labelsBox.setSelected(controller.labelsVisible());
        labelsBox.addActionListener(e -> {
            controller.setLabelsVisible(labelsBox.isSelected());
            onStateChanged.run();
        });

        fpsLabel = new JLabel();
        fpsLabel.setForeground(ThemeColors.CONTROL_FG);
        JSlider fpsSlider = new JSlider(10, 120, 30);
        fpsSlider.setOpaque(false);
        fpsSlider.addChangeListener(e -> {
            int fps = fpsSlider.getValue();
            fpsLabel.setText(I18n.tr(controller.locale(), "controls.fps", fps));
            onFpsChanged.accept(fps);
        });
        fps20 = new JButton("20 FPS");
        fps20.addActionListener(e -> setFpsPreset(fpsSlider, 20));
        fps30 = new JButton("30 FPS");
        fps30.addActionListener(e -> setFpsPreset(fpsSlider, 30));
        fps60 = new JButton("60 FPS");
        fps60.addActionListener(e -> setFpsPreset(fpsSlider, 60));

        perfModeBox = new JCheckBox();
        perfModeBox.setForeground(ThemeColors.CONTROL_FG);
        perfModeBox.setOpaque(false);
        perfModeBox.addActionListener(e -> {
            onPerformanceModeChanged.accept(perfModeBox.isSelected());
            onStateChanged.run();
        });

        zoomLabel = new JLabel();
        zoomLabel.setForeground(ThemeColors.CONTROL_FG);
        JSlider zoomSlider = new JSlider(25, 400, 100);
        zoomSlider.setOpaque(false);
        zoomSlider.addChangeListener(e -> {
            int zoomPercent = zoomSlider.getValue();
            zoomLabel.setText(I18n.tr(controller.locale(), "controls.zoom", zoomPercent));
            onZoomChanged.accept(zoomPercent / 100.0);
            onStateChanged.run();
        });
        zoomFit = new JButton();
        zoomFit.addActionListener(e -> setZoomPreset(zoomSlider, 100));

        languageLabel = new JLabel();
        languageLabel.setForeground(ThemeColors.CONTROL_FG);
        languageBox = new JComboBox<>(new LocaleOption[]{
                new LocaleOption(I18n.EN),
                new LocaleOption(I18n.FR),
                new LocaleOption(I18n.JA)
        });
        lastAppliedLocale = controller.locale();
        languageBox.setRenderer(new DefaultListCellRenderer() {
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
                    label.setText(I18n.tr(controller.locale(), localeLabelKey(option.locale())));
                }
                return label;
            }
        });
        languageBox.setSelectedIndex(Math.max(0, I18n.supportedLocales().indexOf(controller.locale())));
        languageBox.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            applySelectedLocale(speedSlider, tiltSlider, fpsSlider, zoomSlider);
        });
        languageBox.addActionListener(e -> applySelectedLocale(speedSlider, tiltSlider, fpsSlider, zoomSlider));

        add(speedLabel);
        add(Box.createHorizontalStrut(10));
        add(speedSlider);
        add(Box.createHorizontalStrut(8));
        add(preset1);
        add(Box.createHorizontalStrut(4));
        add(preset10);
        add(Box.createHorizontalStrut(4));
        add(preset100);
        add(Box.createHorizontalStrut(14));
        add(pauseButton);
        add(Box.createHorizontalStrut(8));
        add(resetButton);
        add(Box.createHorizontalStrut(14));
        add(tiltBox);
        add(Box.createHorizontalStrut(8));
        add(tiltLabel);
        add(Box.createHorizontalStrut(6));
        add(tiltSlider);
        add(Box.createHorizontalStrut(10));
        add(hoverBox);
        add(Box.createHorizontalStrut(10));
        add(labelsBox);
        add(Box.createHorizontalStrut(10));
        add(fpsLabel);
        add(Box.createHorizontalStrut(6));
        add(fpsSlider);
        add(Box.createHorizontalStrut(6));
        add(fps20);
        add(Box.createHorizontalStrut(4));
        add(fps30);
        add(Box.createHorizontalStrut(4));
        add(fps60);
        add(Box.createHorizontalStrut(10));
        add(perfModeBox);
        add(Box.createHorizontalStrut(10));
        add(zoomLabel);
        add(Box.createHorizontalStrut(6));
        add(zoomSlider);
        add(Box.createHorizontalStrut(6));
        add(zoomFit);
        add(Box.createHorizontalStrut(10));
        add(languageLabel);
        add(Box.createHorizontalStrut(6));
        add(languageBox);

        refreshTexts(speedSlider.getValue(), tiltSlider.getValue(), fpsSlider.getValue(), zoomSlider.getValue());
    }

    private void refreshTexts(int speed, int tilt, int fps, int zoomPercent) {
        speedLabel.setText(I18n.tr(controller.locale(), "controls.speed", speed));
        pauseButton.setText(I18n.tr(controller.locale(), controller.paused() ? "controls.resume" : "controls.pause"));
        resetButton.setText(I18n.tr(controller.locale(), "controls.reset"));
        tiltBox.setText(I18n.tr(controller.locale(), "controls.tilt"));
        tiltLabel.setText(I18n.tr(controller.locale(), "controls.tiltStrength", tilt));
        hoverBox.setText(I18n.tr(controller.locale(), "controls.hoverInfo"));
        labelsBox.setText(I18n.tr(controller.locale(), "controls.labels"));
        fpsLabel.setText(I18n.tr(controller.locale(), "controls.fps", fps));
        perfModeBox.setText(I18n.tr(controller.locale(), "controls.performance"));
        zoomLabel.setText(I18n.tr(controller.locale(), "controls.zoom", zoomPercent));
        zoomFit.setText(I18n.tr(controller.locale(), "controls.fit"));
        languageLabel.setText(I18n.tr(controller.locale(), "controls.language"));
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

    private void setSpeedPreset(JSlider slider, int value) {
        slider.setValue(value);
        controller.setDaysPerSecond(value);
        onStateChanged.run();
    }

    private void setFpsPreset(JSlider slider, int fps) {
        slider.setValue(fps);
        onFpsChanged.accept(fps);
    }

    private void setZoomPreset(JSlider slider, int zoomPercent) {
        slider.setValue(zoomPercent);
        onZoomChanged.accept(zoomPercent / 100.0);
        onStateChanged.run();
    }

    private record LocaleOption(Locale locale) {}
}
