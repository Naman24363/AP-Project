package edu.univ.erp.ui.common;

import edu.univ.erp.service.MaintenanceService;
import java.awt.*;
import javax.swing.*;

public class MaintenanceBanner extends JPanel {
    private final JLabel label = new JLabel();
    private final JLabel iconLabel = new JLabel("⚠");

    public MaintenanceBanner() {
        setLayout(new BorderLayout(10, 0));
        setBackground(new Color(255, 152, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        iconLabel.setForeground(new Color(255, 255, 255));
        add(iconLabel, BorderLayout.WEST);

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.WHITE);
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
