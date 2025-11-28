package edu.univ.erp.ui.student;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.StudentService;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class TimetablePanel extends JPanel {
    private final Session session;
    private final StudentService studentSvc = new StudentService();
    private JPanel contentPanel;
    private StudentDashboard dashboardParent;

    // Modern color palette
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    // Day colors for visual distinction
    private static final Color[] DAY_COLORS = {
            new Color(239, 68, 68), // Monday - Red
            new Color(249, 115, 22), // Tuesday - Orange
            new Color(234, 179, 8), // Wednesday - Yellow
            new Color(34, 197, 94), // Thursday - Green
            new Color(59, 130, 246), // Friday - Blue
            new Color(139, 92, 246) // Saturday - Purple
    };

    private static final String[] DAY_NAMES = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
    private static final String[] DAY_ABBREVS = { "mon", "tue", "wed", "thu", "fri", "sat" };

    public TimetablePanel(Session session) {
        this(session, null);
    }

    public TimetablePanel(Session session, StudentDashboard dashboardParent) {
        this.session = session;
        this.dashboardParent = dashboardParent;
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Content area
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Load initial data
        refresh();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("My Schedule");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Your registered courses organized by day");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BG_COLOR);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    /** Refresh the timetable with the latest registration data */
    public void refresh() {
        contentPanel.removeAll();

        Map<Integer, List<CourseEntry>> coursesByDay = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            coursesByDay.put(i, new ArrayList<>());
        }

        try {
            DefaultTableModel regs = studentSvc.myRegistrations(session.userId);

            for (int r = 0; r < regs.getRowCount(); r++) {
                String dayTime = (String) regs.getValueAt(r, 5);
                String room = (String) regs.getValueAt(r, 6);
                String code = String.valueOf(regs.getValueAt(r, 3));
                String title = String.valueOf(regs.getValueAt(r, 4));
                String instructor = String.valueOf(regs.getValueAt(r, 1));
                String status = String.valueOf(regs.getValueAt(r, 7));

                if (dayTime == null || dayTime.isEmpty())
                    continue;

                String timeOnly = extractTime(dayTime);
                String normDayTime = dayTime.toLowerCase();

                for (int dc = 0; dc < DAY_ABBREVS.length; dc++) {
                    if (normDayTime.contains(DAY_ABBREVS[dc]) || normDayTime.contains(DAY_NAMES[dc].toLowerCase())) {
                        CourseEntry entry = new CourseEntry(code, title, timeOnly, room, instructor, status);
                        coursesByDay.get(dc).add(entry);
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Failed to load registrations for timetable: " + ex.getMessage());
        }

        // Check if there are any courses
        boolean hasCourses = coursesByDay.values().stream().anyMatch(list -> !list.isEmpty());

        if (!hasCourses) {
            contentPanel.add(createEmptyState());
        } else {
            // Create day sections for days that have courses
            for (int i = 0; i < 6; i++) {
                List<CourseEntry> courses = coursesByDay.get(i);
                if (!courses.isEmpty()) {
                    contentPanel.add(createDaySection(DAY_NAMES[i], courses, DAY_COLORS[i]));
                    contentPanel.add(Box.createVerticalStrut(16));
                }
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createEmptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(60, 40, 60, 40));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("\uD83D\uDCDA");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("No courses registered yet");
        message.setFont(new Font("Segoe UI", Font.BOLD, 18));
        message.setForeground(TEXT_PRIMARY);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Register for courses in the Catalog tab to see them here");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hint.setForeground(TEXT_SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(icon);
        panel.add(Box.createVerticalStrut(16));
        panel.add(message);
        panel.add(Box.createVerticalStrut(8));
        panel.add(hint);
        panel.add(Box.createVerticalGlue());

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_COLOR);
        wrapper.add(panel);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        return wrapper;
    }

    private JPanel createDaySection(String dayName, List<CourseEntry> courses, Color dayColor) {
        // Sort courses by time (earlier classes first)
        courses.sort((a, b) -> {
            int timeA = parseTimeToMinutes(a.time);
            int timeB = parseTimeToMinutes(b.time);
            return Integer.compare(timeA, timeB);
        });

        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(BG_COLOR);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Day header
        JPanel dayHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dayHeader.setBackground(BG_COLOR);
        dayHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        dayHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Color indicator
        JPanel colorIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dayColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        colorIndicator.setPreferredSize(new Dimension(4, 24));
        colorIndicator.setOpaque(false);

        JLabel dayLabel = new JLabel("  " + dayName);
        dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dayLabel.setForeground(TEXT_PRIMARY);

        JLabel countLabel = new JLabel("  (" + courses.size() + " class" + (courses.size() > 1 ? "es" : "") + ")");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(TEXT_SECONDARY);

        dayHeader.add(colorIndicator);
        dayHeader.add(dayLabel);
        dayHeader.add(countLabel);

        section.add(dayHeader);
        section.add(Box.createVerticalStrut(12));

        // Course cards container
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(BG_COLOR);
        cardsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < courses.size(); i++) {
            cardsContainer.add(createCourseCard(courses.get(i), dayColor));
            if (i < courses.size() - 1) {
                cardsContainer.add(Box.createVerticalStrut(10));
            }
        }

        section.add(cardsContainer);
        return section;
    }

    private JPanel createCourseCard(CourseEntry course, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background with rounded corners
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Border
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // Left accent stripe
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 5, getHeight(), 12, 12);
                g2.fillRect(5, 0, 3, getHeight());

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left section - Course info
        JPanel leftSection = new JPanel();
        leftSection.setLayout(new BoxLayout(leftSection, BoxLayout.Y_AXIS));
        leftSection.setOpaque(false);

        JLabel codeLabel = new JLabel(course.code);
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        codeLabel.setForeground(accentColor);

        JLabel titleLabel = new JLabel(course.title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel instructorLabel = new JLabel("\uD83D\uDC64 " + course.instructor);
        instructorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instructorLabel.setForeground(TEXT_SECONDARY);

        leftSection.add(codeLabel);
        leftSection.add(Box.createVerticalStrut(2));
        leftSection.add(titleLabel);
        leftSection.add(Box.createVerticalStrut(6));
        leftSection.add(instructorLabel);

        // Right section - Time and room
        JPanel rightSection = new JPanel();
        rightSection.setLayout(new BoxLayout(rightSection, BoxLayout.Y_AXIS));
        rightSection.setOpaque(false);
        rightSection.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel timeBadge = createBadge("\uD83D\uDD50 " + (course.time.isEmpty() ? "TBA" : course.time),
                new Color(239, 246, 255), PRIMARY_COLOR);
        timeBadge.setAlignmentX(Component.RIGHT_ALIGNMENT);

        String roomText = course.room == null || course.room.isEmpty() ? "TBA" : course.room;
        JPanel roomBadge = createBadge("\uD83D\uDCCD " + roomText,
                new Color(240, 253, 244), new Color(34, 197, 94));
        roomBadge.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightSection.add(timeBadge);
        rightSection.add(Box.createVerticalStrut(6));
        rightSection.add(roomBadge);

        card.add(leftSection, BorderLayout.CENTER);
        card.add(rightSection, BorderLayout.EAST);

        return card;
    }

    private JPanel createBadge(String text, Color bgColor, Color textColor) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        badge.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(textColor);
        badge.add(label);

        return badge;
    }

    private String extractTime(String dayTime) {
        if (dayTime == null)
            return "";
        // Match time pattern with AM/PM: "10:00-11:00 AM" or "10:00-11:00 PM"
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d{1,2}:\\d{2}\\s*-\\s*\\d{1,2}:\\d{2})\\s*(AM|PM)?",
                        java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(dayTime);
        if (m.find()) {
            String timeRange = m.group(1).trim();
            String ampm = m.group(2);
            if (ampm != null && !ampm.isEmpty()) {
                return timeRange + " " + ampm.toUpperCase();
            }
            return timeRange;
        }
        m = java.util.regex.Pattern.compile("(\\d{1,2}:\\d{2})").matcher(dayTime);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    /** Parse time string to minutes from midnight for sorting */
    private int parseTimeToMinutes(String time) {
        if (time == null || time.isEmpty())
            return Integer.MAX_VALUE; // Unknown times go to the end

        try {
            // Extract start time and AM/PM
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(\\d{1,2}):(\\d{2}).*?(AM|PM)?", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(time);
            if (m.find()) {
                int hours = Integer.parseInt(m.group(1));
                int minutes = Integer.parseInt(m.group(2));
                String ampm = m.group(3);

                // Convert to 24-hour format for proper sorting
                if (ampm != null) {
                    ampm = ampm.toUpperCase();
                    if (ampm.equals("PM") && hours != 12) {
                        hours += 12;
                    } else if (ampm.equals("AM") && hours == 12) {
                        hours = 0;
                    }
                }
                return hours * 60 + minutes;
            }
        } catch (NumberFormatException e) {
            // Ignore, return max value
        }
        return Integer.MAX_VALUE;
    }

    // Inner class to hold course data
    private static class CourseEntry {
        final String code;
        final String title;
        final String time;
        final String room;
        final String instructor;
        final String status;

        CourseEntry(String code, String title, String time, String room, String instructor, String status) {
            this.code = code;
            this.title = title;
            this.time = time;
            this.room = room;
            this.instructor = instructor;
            this.status = status;
        }
    }
}
