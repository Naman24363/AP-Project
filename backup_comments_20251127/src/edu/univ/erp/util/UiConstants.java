package edu.univ.erp.util;

import java.awt.*;

/**
 * Centralized UI Constants and helper methods for consistent UI styling across
 * the application.
 */
public class UiConstants {
    // Modern Material Design-inspired color palette
    public static final Color PRIMARY = new Color(25, 118, 210); // Deep Blue
    public static final Color PRIMARY_DARK = new Color(13, 71, 161); // Darker Blue
    public static final Color PRIMARY_LIGHT = new Color(66, 165, 245); // Light Blue
    public static final Color ACCENT = new Color(0, 188, 212); // Cyan
    public static final Color SUCCESS = new Color(56, 142, 60); // Green
    public static final Color SUCCESS_LIGHT = new Color(129, 199, 132); // Light Green
    public static final Color ERROR = new Color(211, 47, 47); // Red
    public static final Color ERROR_LIGHT = new Color(244, 67, 54); // Light Red
    public static final Color WARNING = new Color(251, 140, 0); // Orange
    public static final Color WARNING_LIGHT = new Color(255, 152, 0); // Light Orange
    public static final Color INFO = new Color(0, 172, 193); // Teal
    public static final Color BG_LIGHT = new Color(248, 248, 248); // Almost white
    public static final Color BG_SURFACE = Color.WHITE;
    public static final Color TEXT_DARK = new Color(33, 33, 33); // Dark text
    public static final Color TEXT_LIGHT = new Color(117, 117, 117); // Medium Gray
    public static final Color TEXT_HINT = new Color(189, 189, 189); // Light Gray
    public static final Color DIVIDER = new Color(224, 224, 224); // Divider line
    public static final Color HOVER = new Color(245, 245, 245); // Hover background
    public static final Color SHADOW = new Color(0, 0, 0, 30); // Shadow

    // Font constants
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 10);

    // Padding and spacing
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 12;
    public static final int PADDING_LARGE = 16;
    public static final int PADDING_XL = 20;

    // Animation timings (in milliseconds)
    public static final int ANIMATION_DURATION_SHORT = 150;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;
}
