package com.fitpulse.ui.theme;

import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Modern color palette, typography tokens, and border styles for FitPulse.
 */
public final class UITheme {

    private UITheme() {}

    // Backgrounds
    public static final Color BG_DARK = new Color(15, 23, 42);       // #0F172A
    public static final Color BG_CARD = new Color(30, 41, 59);       // #1E293B
    public static final Color BG_CARD_HOVER = new Color(45, 60, 85);
    public static final Color BG_SIDEBAR = new Color(11, 17, 32);    // #0B1120
    public static final Color BG_INPUT = new Color(24, 34, 52);

    // Accent Colors
    public static final Color ACCENT_PRIMARY = new Color(99, 102, 241); // Indigo #6366F1
    public static final Color ACCENT_TEAL = new Color(13, 148, 136);   // Teal #0D9488
    public static final Color ACCENT_SUCCESS = new Color(16, 185, 129);// Emerald #10B981
    public static final Color ACCENT_WARNING = new Color(245, 158, 11);// Amber #F59E0B
    public static final Color ACCENT_DANGER = new Color(239, 68, 68);  // Rose #EF4444

    // Foregrounds / Text
    public static final Color TEXT_MAIN = new Color(248, 250, 252);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);
    public static final Color BORDER_COLOR = new Color(51, 65, 85);

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        );
    }
}
