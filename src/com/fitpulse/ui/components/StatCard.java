package com.fitpulse.ui.components;

import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * Modern KPI Stat Card for metrics presentation.
 */
public class StatCard extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(8, 8));
        setBackground(UITheme.BG_CARD);
        setBorder(UITheme.createCardBorder());

        // Header / Title
        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(accentColor);

        // Value
        lblValue = new JLabel(value);
        lblValue.setFont(UITheme.FONT_CARD_VALUE);
        lblValue.setForeground(UITheme.TEXT_MAIN);

        // Subtitle
        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(6));
        content.add(lblValue);
        content.add(Box.createVerticalStrut(4));
        content.add(lblSubtitle);

        add(content, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        // Subtle top accent line
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, getWidth(), 4, 16, 16);
        g2.dispose();
    }
}
