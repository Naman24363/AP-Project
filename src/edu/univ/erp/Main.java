package edu.univ.erp;

import edu.univ.erp.ui.auth.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Ensure our UI helpers (and look-and-feel) initialize before creating any
        // Swing windows
        edu.univ.erp.util.Ui.init();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
// comment added for main