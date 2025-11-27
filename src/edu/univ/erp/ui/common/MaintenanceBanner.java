package edu.univ.erp.ui.common;

import edu.univ.erp.service.MaintenanceService;
import java.awt.*;
import javax.swing.*;

public class MaintenanceBanner extends JPanel {
    private final JLabel label = new JLabel();
    private final JLabel iconLabel = new JLabel("⚠");

    public MaintenanceBanner() {
        setLayout(new BorderLayout(10, 0));
        setBackground(new Color(255, 193, 7));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconLabel.setForeground(new Color(245, 127, 23));
        add(iconLabel, BorderLayout.WEST);

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(33, 33, 33));
        add(label, BorderLayout.CENTER);

        refresh();
    }

    public final void refresh() {
        boolean on = MaintenanceService.isMaintenanceOn();
        if (on) {
            label.setText("🔧 Maintenance Mode is ON — Students and Instructors can view only. No changes allowed.");
            setVisible(true);
        } else {
            label.setText("");
            setVisible(false);
        }
    }
}
