package edu.univ.erp.ui.student;

import edu.univ.erp.auth.Session;
import edu.univ.erp.util.Ui;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class TimetablePanel extends JPanel {
    private final Session session;

    public TimetablePanel(Session session) {
        this.session = session;
        setLayout(new BorderLayout(8, 8));

        String[] cols = new String[] { "Time", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Time rows (ordered) — fill cells with strings where schedule exists
        String[][] rows = new String[][] {
                { "8:30–9:30", "", "", "Tut OS (C101 Sec A, C201 Sec B)", "", "", "" },
                { "8:30–10:30", "DM (B003)\nDS (C102)\nCTD (A006)", "", "DM (B003)\nDS (C102)\nCTD (A006)", "", "",
                        "" },
                { "8:30–11:30", "", "Lab ELD (A006)", "", "Lab GMB + IQB (L320,L321)\nLab RMSSD (C01)", "", "" },
                { "9:00–10:00", "", "RA-I (C21)", "", "RA-I (C21)", "", "" },
                { "9:30–10:30", "", "", "DM (B003)\nDS (C102)\nCTD (A006)", "", "S&S (C102)\nFOB II (B003)", "" },
                { "10:00–10:30", "", "Slot 1", "", "Slot 1", "", "" },
                { "10:30–11:30", "Slot 4", "", "Slot 4", "", "", "" },
                { "11:00–12:00", "DPP (A106)\nELD (A006)", "FOE (C212)\nRMSSD (C11)\nIQB (A006)",
                        "S&S (C102)\nFOB II (B003)", "DPP (A106)\nELD (A006)", "RMSSD (C11)\nIQB (A006)\nFOE (C212)",
                        "" },
                { "12:00–12:30", "Slot 2", "", "", "", "", "" },
                { "1:00–2:30", "", "", "", "Tut M-III (A007,B105,A006,...)", "", "" },
                { "1:30–2:30", "", "Tut NT (C03,C13)", "", "", "DPP Practice (A106)", "" },
                { "1:30–3:30", "Tut DS (All Groups) – C03,C13,C22,C24,...", "", "Tut AP (All Groups) – (many groups)",
                        "", "", "Lab CTD (L302,L303)" },
                { "2:00–3:00", "Tut CTD (C210,C211,...)\nTut DM (C01,C11)", "", "", "", "", "" },
                { "2:30–3:30", "", "", "", "Tut RA-I (C22,C24,C03,C13)", "", "" },
                { "3:00–4:00", "OS-A (C201)\nOS-B (C102)", "NT (C12)\nAP (B003)", "OS-A (C201)\nOS-B (C101)",
                        "NT (C12)\nAP (B003)", "", "" },
                { "3:30–4:30", "", "", "", "", "Faculty Meeting Slot", "" },
                { "4:00–5:00", "", "M-III Sec A (C201)\nM-III Sec B (B003)", "SOE (C02)",
                        "M-III Sec A (C201)\nM-III Sec B (B003)", "", "" },
                { "4:00–5:30", "SOE (C02)", "", "", "", "", "" },
                { "4:30–6:00", "", "", "", "", "", "Seminar Slot" },
                { "5:00–5:30", "", "SPP (C21)", "", "SPP (C21)", "", "" }
        };

        for (String[] r : rows)
            model.addRow(r);

        // build continuation map: if a day column has identical content in consecutive
        // rows,
        // mark the later rows as continuation so renderer can hide the top border
        // (visual merge)
        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();
        boolean[][] continuation = new boolean[rowCount][colCount];
        for (int c = 1; c < colCount; c++) {
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
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        for (int i = 1; i < table.getColumnCount(); i++)
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
