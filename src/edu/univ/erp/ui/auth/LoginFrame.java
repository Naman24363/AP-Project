package edu.univ.erp.ui.auth;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.Session;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.common.MaintenanceBanner;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentHome;
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

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(30, 16, 30, 16));

        JPanel inner = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(new Color(224, 224, 224));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, Math.max(0, w - 1), Math.max(0, h - 1), arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(20, 36, 20, 36));
        inner.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel("IIIT DELHI ERP");
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
            }
        }
        if (headerFont == null) {
            try {
                headerFont = new Font("Oswald", Font.BOLD, 40);
            } catch (Exception ex) {
                headerFont = new Font("Segoe UI", Font.BOLD, 40);
            }
        }
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(titleLabel);
        inner.add(Box.createVerticalStrut(12));

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

        JLabel passLabel = Ui.createLabelBold("Password:");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(passLabel);
        inner.add(Box.createVerticalStrut(5));
        txtPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtPass.setPreferredSize(new Dimension(300, txtPass.getPreferredSize().height));
        txtPass.setMaximumSize(smallSize);
        inner.add(txtPass);
        inner.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton btnLogin = Ui.button("  Login  ", this::doLogin);
        JButton btnExit = Ui.buttonSecondary("  Exit  ", this::dispose);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));
        inner.add(buttonPanel);

        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Ui.BG_LIGHT);
        footerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel infoLabel = new JLabel("Test Accounts: admin1, inst1, stu1, stu2");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        infoLabel.setForeground(Ui.TEXT_LIGHT);
        footerPanel.add(infoLabel);
        formPanel.add(inner, new GridBagConstraints());

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(new BorderLayout());
        bgPanel.add(banner, BorderLayout.NORTH);
        bgPanel.add(formPanel, BorderLayout.CENTER);
        bgPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(bgPanel);

        txtPass.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });
    }

    private class BackgroundPanel extends JPanel {
        private BufferedImage bgImage;

        BackgroundPanel() {
            try {
                File rootImage = new File("WhatsApp Image 2025-11-26 at 19.23.27_9b7e1506.jpg");
                if (rootImage.exists()) {
                    try {
                        bgImage = ImageIO.read(rootImage);
                        if (bgImage != null)
                            System.out.println("LoginFrame: loaded background from " + rootImage.getAbsolutePath());
                    } catch (IOException ignore) {
                        bgImage = null;
                    }
                }

                if (bgImage == null) {
                    InputStream is = getClass().getResourceAsStream("/images/login_bg.jpg");
                    if (is != null) {
                        bgImage = ImageIO.read(is);
                        if (bgImage != null)
                            System.out.println("LoginFrame: loaded background from classpath /images/login_bg.jpg");
                    }
                }

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
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            } else {
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
                        "LoginFrame: background image not loaded (looked for WhatsApp Image 2025-11-26 at 19.23.27_9b7e1506.jpg and resources/classpath).\n");
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
                case "STUDENT" -> new StudentHome(s);
                case "INSTRUCTOR" -> new InstructorDashboard(s);
                default -> new AdminDashboard(s);
            };

            dash.setVisible(true);
            dispose();
        } catch (Exception e) {
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
