package com.fitpulse.ui.components;

import com.fitpulse.ui.theme.UITheme;
import java.awt.*;
import javax.swing.*;

/**
 * Custom 2D painted circular progress indicator for goal visualization.
 */
public class CircularProgressBar extends JComponent {
    private double progress = 0.0; // 0 to 100
    private Color progressColor = UITheme.ACCENT_TEAL;
    private Color trackColor = new Color(51, 65, 85);
    private int strokeWidth = 10;

    public CircularProgressBar() {
        setPreferredSize(new Dimension(110, 110));
    }

    public void setProgress(double progress) {
        this.progress = Math.min(100.0, Math.max(0.0, progress));
        repaint();
    }

    public void setProgressColor(Color color) {
        this.progressColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - strokeWidth * 2;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        // Draw track
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(trackColor);
        g2.drawOval(x, y, size, size);

        // Draw arc progress
        int arcAngle = (int) Math.round((progress / 100.0) * 360.0);
        g2.setColor(progressColor);
        g2.drawArc(x, y, size, size, 90, -arcAngle);

        // Draw center percentage text
        String text = String.format("%.0f%%", progress);
        g2.setFont(UITheme.FONT_SUBTITLE);
        g2.setColor(UITheme.TEXT_MAIN);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(text)) / 2;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}
