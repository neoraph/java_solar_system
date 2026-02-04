package com.raph.solarsystem.view;

import com.raph.solarsystem.controller.SolarSystemController;
import com.raph.solarsystem.model.SolarSystemModel;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ControlsPanelLocaleTest {
    @Test
    void selectingLanguageUpdatesControllerLocaleAndTexts() throws Exception {
        SolarSystemController controller = new SolarSystemController(new SolarSystemModel());
        AtomicReference<ControlsPanel> panelRef = new AtomicReference<>();
        AtomicReference<JComboBox<?>> boxRef = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            ControlsPanel panel = new ControlsPanel(
                    controller,
                    () -> {},
                    fps -> {},
                    enabled -> {},
                    zoom -> {},
                    locale -> {}
            );
            panelRef.set(panel);
            boxRef.set(findLanguageBox(panel));
        });

        JComboBox<?> languageBox = boxRef.get();
        assertNotNull(languageBox);

        SwingUtilities.invokeAndWait(() -> languageBox.setSelectedIndex(1));
        assertEquals(Locale.FRENCH.getLanguage(), controller.locale().getLanguage());

        SwingUtilities.invokeAndWait(() -> languageBox.setSelectedIndex(2));
        assertEquals(Locale.JAPANESE.getLanguage(), controller.locale().getLanguage());
    }

    private JComboBox<?> findLanguageBox(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JComboBox<?> box) {
                return box;
            }
        }
        return null;
    }
}
