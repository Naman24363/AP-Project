package edu.univ.erp.ui.auth;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.Session;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.common.MaintenanceBanner;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;
import edu.univ.erp.util.Ui;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private final JTextField txtUser = Ui.createTextField(20);
    private final JPasswordField txtPass = Ui.createPasswordField(20);
    private final AuthService auth = new AuthService();
    private final MaintenanceBanner banner = new MaintenanceBanner();

    public LoginFrame() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setResizable(true);

        // (topBanner removed) Title label will be shown inside the centered login box

        // Login form panel — centered box with a visible border
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(30, 16, 30, 16));

        // Inner box (fixed max width) that will contain the labels, fields and buttons
        JPanel inner = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;
                int w = getWidth();
                int h = getHeight();
                // fill background rounded
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                // draw border
                g2.setColor(new Color(224, 224, 224));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        // keep padding inside the rounded panel
        inner.setBorder(new EmptyBorder(20, 36, 20, 36));
        // Limit width so it looks like a box, even on very wide screens
        inner.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));

        // Title label (will be moved into the centered login box)
        JLabel titleLabel = new JLabel("IIIT DELHI ERP");
        // Attempt to load Oswald from bundled font files (put TTFs under
        // src/main/resources/fonts)
        Font headerFont = null;
        String[] candidates = { "/fonts/Oswald-Bold.ttf", "/fonts/Oswald-SemiBold.ttf", "/fonts/Oswald-Regular.ttf" };
        for (String c : candidates) {
            try (InputStream fis = getClass().getResourceAsStream(c)) {
                if (fis != null) {
                    headerFont = Font.createFont(Font.TRUETYPE_FONT, fis).deriveFont(Font.PLAIN, 40f);
                    System.out.println("LoginFrame: loaded header font from resource " + c);
                    break;
                }
            } catch (Exception ex) {
                // ignore and try next candidate
            }
        }
        if (headerFont == null) {
            // fallback: try OS-installed Oswald, otherwise Segoe UI
            try {
                headerFont = new Font("Oswald", Font.BOLD, 40);
            } catch (Exception ex) {
                headerFont = new Font("Segoe UI", Font.BOLD, 40);
            }
        }
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.BLACK);
        // Place the title at the top of the inner (centered) login box
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(titleLabel);
        inner.add(Box.createVerticalStrut(12));

        // Username
        JLabel userLabel = Ui.createLabelBold("Username:");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(userLabel);
        inner.add(Box.createVerticalStrut(5));
        txtUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension smallSize = new Dimension(360, txtUser.getPreferredSize().height);
        txtUser.setPreferredSize(new Dimension(300, txtUser.getPreferredSize().height));
        txtUser.setMaximumSize(smallSize);
        inner.add(txtUser);
        inner.add(Box.createVerticalStrut(15));

        // Password
        JLabel passLabel = Ui.createLabelBold("Password:");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(passLabel);
        inner.add(Box.createVerticalStrut(5));
        txtPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtPass.setPreferredSize(new Dimension(300, txtPass.getPreferredSize().height));
        txtPass.setMaximumSize(smallSize);
        inner.add(txtPass);
        inner.add(Box.createVerticalStrut(20));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnLogin = Ui.button("  Login  ", this::doLogin);
        JButton btnExit = Ui.buttonSecondary("  Exit  ", this::dispose);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));
        inner.add(buttonPanel);

        // Footer with info
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Ui.BG_LIGHT);
        footerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel infoLabel = new JLabel("Test Accounts: admin1, inst1, stu1, stu2");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        infoLabel.setForeground(Ui.TEXT_LIGHT);
        footerPanel.add(infoLabel);
        // Add inner to centered outer panel
        formPanel.add(inner, new GridBagConstraints());

        // Use a background panel that paints an image; place UI components on top
        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(new BorderLayout());
        bgPanel.add(banner, BorderLayout.NORTH);
        bgPanel.add(formPanel, BorderLayout.CENTER);
        bgPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(bgPanel);

        // Enable Enter key for login
        txtPass.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });
    }

    /**
     * Simple panel that will draw a background image
     * Try classpath resource `/images/login_bg.jpg` first, then
     * `resources/login_bg.jpg` on disk.
     */
    private class BackgroundPanel extends JPanel {
        private BufferedImage bgImage;

        BackgroundPanel() {
            try {
                // First try a user-specified absolute path (your provided JPG file)
                File userJpg = new File("C:\\Users\\prate\\OneDrive\\Pictures\\unnamed.jpg");
                if (userJpg.exists()) {
                    try {
                        bgImage = ImageIO.read(userJpg);
                        if (bgImage != null)
                            System.out.println("LoginFrame: loaded background from " + userJpg.getAbsolutePath());
                    } catch (IOException ignore) {
                        bgImage = null;
                    }
                }

                // Next try classpath resource
                if (bgImage == null) {
                    InputStream is = getClass().getResourceAsStream("/images/login_bg.jpg");
                    if (is != null) {
                        bgImage = ImageIO.read(is);
                        if (bgImage != null)
                            System.out.println("LoginFrame: loaded background from classpath /images/login_bg.jpg");
                    }
                }

                // Lastly try repository resource path
                if (bgImage == null) {
                    File f = new File("resources/login_bg.jpg");
                    if (f.exists()) {
                        bgImage = ImageIO.read(f);
                        if (bgImage != null)
                            System.out.println("LoginFrame: loaded background from resources/login_bg.jpg");
                    }
                }

            } catch (IOException ex) {
                bgImage = null;
            }
            if (bgImage != null) {
                System.out.println("LoginFrame: background size=" + bgImage.getWidth() + "x" + bgImage.getHeight());
            } else {
                System.out.println("LoginFrame: no background image loaded after checks");
            }
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                // Draw the image scaled to fill the panel
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Visible debug hint so you know loading failed
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                String msg = "Background image not found or unsupported format";
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(msg);
                int x = (getWidth() - w) / 2;
                int y = getHeight() / 2;
                g2.drawString(msg, x, y);
                g2.dispose();
                System.out.println(
                        "LoginFrame: background image not loaded (looked for C:\\Users\\prate\\OneDrive\\Pictures\\unnamed.jpg, C:\\Users\\prate\\Downloads\\unnamed.webp, and resources/classpath).\n");
            }
        }
    }

    private void doLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            Ui.msgError(this, "Please enter both username and password.");
            return;
        }

        try {
            System.out.println("LoginFrame: attempting login for user='" + username + "'");
            Session s = auth.login(username, password);
            System.out.println("LoginFrame: auth.login returned Session role=" + (s == null ? "<null>" : s.role));
            Session.set(s);

            JFrame dash = switch (s.role) {
                case "STUDENT" -> new StudentDashboard(s);
                case "INSTRUCTOR" -> new InstructorDashboard(s);
                default -> new AdminDashboard(s);
            };

            dash.setVisible(true);
            dispose();
        } catch (Exception e) {
            // show full error and log stack for debugging
            String msg = e.getMessage();
            System.err.println("LoginFrame: login failed: " + msg);
            e.printStackTrace(System.err);
            Ui.msgError(this, msg == null ? "Login failed (see console)" : msg);
            txtPass.setText("");
            txtUser.requestFocus();
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (b)
            banner.refresh();
        super.setVisible(b);
    }
}
