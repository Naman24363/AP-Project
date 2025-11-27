package edu.univ.erp.ui.student;

import edu.univ.erp.auth.Session;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.Ui;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class TimetablePanel extends JPanel {
    private final Session session;

    public TimetablePanel(Session session) {
        this.session = session;
        setLayout(new BorderLayout(8, 8));

        // Remove the left Time column; keep only day columns
        String[] cols = new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Time rows (ordered) — base rows with empty day cells; we'll populate from DB
        String[] times = new String[] { "8:30–9:30", "8:30–10:30", "8:30–11:30", "9:00–10:00", "9:30–10:30",
                "10:00–10:30", "10:30–11:30", "11:00–12:00", "12:00–12:30", "1:00–2:30", "1:30–2:30",
                "1:30–3:30", "2:00–3:00", "2:30–3:30", "3:00–4:00", "3:30–4:30", "4:00–5:00", "4:00–5:30",
                "4:30–6:00", "5:00–5:30" };

        for (String t : times) {
            Object[] row = new Object[6];
            // no time label column; keep only day cells
            for (int i = 0; i < 6; i++)
                row[i] = "";
            model.addRow(row);
        }

        // populate from student's registrations
        StudentService studentSvc = new StudentService();
        try {
            DefaultTableModel regs = studentSvc.myRegistrations(session.userId);
            // prepare numeric start minutes for timetable rows
            int[] slotStarts = new int[times.length];
            for (int i = 0; i < times.length; i++) {
                slotStarts[i] = parseStartMinutes(times[i]);
            }
            for (int r = 0; r < regs.getRowCount(); r++) {
                // StudentService.myRegistrations currently returns columns:
                // 0: Enrollment ID, 1: Instructor, 2: Section ID, 3: Code, 4: Title,
                // 5: Day/Time, 6: Room, 7: Status
                String dayTime = (String) regs.getValueAt(r, 5); // Day/Time
                String room = (String) regs.getValueAt(r, 6); // Room
                String code = String.valueOf(regs.getValueAt(r, 3));
                String title = String.valueOf(regs.getValueAt(r, 4));
                String display = code + " " + title + (room == null || room.isEmpty() ? "" : " (" + room + ")");

                if (dayTime == null)
                    continue;
                String norm = dayTime == null ? "" : dayTime.toLowerCase();
                // for each day column, check if dayTime mentions the day
                String[] days = new String[] { "mon", "tue", "wed", "thu", "fri", "sat" };
                // parse start minutes from the registration dayTime
                int regStart = parseStartMinutes(dayTime);
                for (int dc = 0; dc < days.length; dc++) {
                    String d = days[dc];
                    if (norm.contains(d) || norm.contains(fullDayName(d))) {
                        // find best matching time row by numeric proximity
                        int bestIdx = -1;
                        int bestDiff = Integer.MAX_VALUE;
                        for (int tr = 0; tr < times.length; tr++) {
                            int slot = slotStarts[tr];
                            if (slot <= 0)
                                continue;
                            int diff = Math.abs(slot - regStart);
                            if (diff < bestDiff) {
                                bestDiff = diff;
                                bestIdx = tr;
                            }
                        }
                        if (bestIdx >= 0 && bestDiff <= 60) { // within 60 minutes
                            Object cur = model.getValueAt(bestIdx, dc);
                            String slotTime = times[bestIdx];
                            String entry = display
                                    + (slotTime == null || slotTime.isEmpty() ? "" : "  (" + slotTime + ")");
                            String newVal = (cur == null || cur.toString().isEmpty()) ? entry
                                    : cur.toString() + "\n" + entry;
                            model.setValueAt(newVal, bestIdx, dc);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Failed to load registrations for timetable: " + ex.getMessage());
        }

        // build continuation map: if a day column has identical content in consecutive
        // rows, mark the later rows as continuation so renderer can hide the top border
        // (visual merge)
        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();
        boolean[][] continuation = new boolean[rowCount][colCount];
        for (int c = 0; c < colCount; c++) {
            for (int r = 0; r < rowCount - 1; r++) {
                Object cur = model.getValueAt(r, c);
                Object nxt = model.getValueAt(r + 1, c);
                if (cur != null && nxt != null) {
                    String a = cur.toString().trim();
                    String b = nxt.toString().trim();
                    if (!a.isEmpty() && a.equals(b)) {
                        continuation[r + 1][c] = true;
                        model.setValueAt("", r + 1, c);
                    }
                }
            }
        }

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new MultiLineCellRenderer(continuation));
        table.setRowHeight(48);

        // column widths
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(260);

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.add(Ui.button("Refresh", () -> {
            // static data; no-op
        }));
        add(south, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> adjustRowHeights(table));
    }

    private void adjustRowHeights(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int maxHeight = table.getRowHeight();
            for (int col = 0; col < table.getColumnCount(); col++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = renderer.getTableCellRendererComponent(table, table.getValueAt(row, col), false, false,
                        row, col);
                int h = comp.getPreferredSize().height + table.getRowMargin();
                maxHeight = Math.max(maxHeight, h);
            }
            if (table.getRowHeight(row) != maxHeight)
                table.setRowHeight(row, maxHeight);
        }
        table.revalidate();
        table.repaint();
    }

    // Normalize day/time strings for simple matching: lower-case, normalize dashes,
    // remove spaces
    private String normalize(String s) {
        if (s == null)
            return "";
        String r = s.replace('\u2013', '-').replace('\u2014', '-').replace('–', '-');
        r = r.replaceAll("\\s+", "");
        return r.toLowerCase();
    }

    private int parseStartMinutes(String s) {
        if (s == null)
            return -1;
        // try to find hh:mm
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}:\\d{2})").matcher(s);
        if (m.find()) {
            String t = m.group(1);
            String[] parts = t.split(":");
            try {
                int h = Integer.parseInt(parts[0]);
                int mm = Integer.parseInt(parts[1]);
                return h * 60 + mm;
            } catch (Exception e) {
                return -1;
            }
        }
        // fallback: lone number
        m = java.util.regex.Pattern.compile("(\\d{1,2})").matcher(s);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1)) * 60;
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    private String fullDayName(String abbr) {
        switch (abbr.toLowerCase()) {
            case "mon":
                return "monday";
            case "tue":
                return "tuesday";
            case "wed":
                return "wednesday";
            case "thu":
                return "thursday";
            case "fri":
                return "friday";
            case "sat":
                return "saturday";
        }
        return abbr;
    }

    private static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
        private final boolean[][] continuation;

        MultiLineCellRenderer(boolean[][] continuation) {
            this.continuation = continuation;
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(value == null ? "" : value.toString());
            if (isSelected)
                setBackground(table.getSelectionBackground());
            else
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            setFont(table.getFont());

            boolean cont = false;
            if (row >= 0 && row < continuation.length && column >= 0 && column < continuation[row].length) {
                cont = continuation[row][column];
            }
            if (cont) {
                setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY));
            } else {
                setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
            }
            return this;
        }
    }
}
