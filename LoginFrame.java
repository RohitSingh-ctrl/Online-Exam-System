import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

// Login screen and Register dialog
public class LoginFrame extends JFrame {

    private final JTextField     tfUser = UIHelper.field();
    private final JPasswordField tfPass = UIHelper.passField();
    private final JLabel         lblMsg = new JLabel(" ");

    public LoginFrame() {
        setTitle("Online Exam System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 400); setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIHelper.BG);
        setLayout(new GridBagLayout());
        build();
    }

    private void build() {
        // Top banner
        JPanel banner = new JPanel(new GridLayout(1, 1));
        banner.setBackground(UIHelper.PRIMARY);
        banner.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel t1 = new JLabel("Online Exam System");
        t1.setFont(new Font("SansSerif", Font.BOLD, 18)); t1.setForeground(UIHelper.WHITE);
        banner.add(t1); 

        // Card
        JPanel card = UIHelper.card("Login");
        card.setPreferredSize(new Dimension(340, 230));

        lblMsg.setFont(UIHelper.F_SMALL); lblMsg.setForeground(UIHelper.DANGER);

        JButton btnLogin    = UIHelper.btn("Login",    UIHelper.PRIMARY);
        JButton btnRegister = UIHelper.btn("Register", UIHelper.ACCENT);

        for (JComponent c : new JComponent[]{
                UIHelper.row("Username", tfUser),
                UIHelper.row("Password", tfPass),
                lblMsg, btnLogin, btnRegister}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            card.add(c);
            card.add(Box.createVerticalStrut(7));
        }

        JPanel wrap = new JPanel(new BorderLayout(0, 16));
        wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(20, 24, 20, 24));
        wrap.add(banner, BorderLayout.NORTH);
        wrap.add(card,   BorderLayout.CENTER);
        add(wrap);

        btnLogin.addActionListener(e -> doLogin());
        btnRegister.addActionListener(e -> new RegisterDialog(this).setVisible(true));
    }

    private void doLogin() {
        String user = tfUser.getText().trim();
        String pass = new String(tfPass.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            lblMsg.setText("Username and password are required."); return;
        }
        try {
            PreparedStatement ps = DBConnection.get().prepareStatement(
                "SELECT id, full_name, role FROM users WHERE username=? AND password=?");
            ps.setString(1, user); ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id"), role_col = 0;
                String name = rs.getString("full_name"), role = rs.getString("role");
                rs.close(); ps.close(); dispose();
                if ("ADMIN".equals(role)) new AdminFrame(name).setVisible(true);
                else                      new StudentFrame(id, name).setVisible(true);
            } else {
                lblMsg.setText("Invalid username or password.");
            }
        } catch (SQLException ex) { lblMsg.setText("DB error: " + ex.getMessage()); }
    }

    // ── Register dialog ──────────────────────────────────────────────────────
    static class RegisterDialog extends JDialog {
        private final JTextField     tfUser  = UIHelper.field();
        private final JPasswordField tfPass  = UIHelper.passField();
        private final JTextField     tfName  = UIHelper.field();
        private final JTextField     tfEmail = UIHelper.field();
        private final JLabel         lblMsg  = new JLabel(" ");

        RegisterDialog(JFrame parent) {
            super(parent, "Register New Student", true);
            setSize(380, 320); setResizable(false);
            setLocationRelativeTo(parent);
            getContentPane().setBackground(UIHelper.BG);

            JPanel card = UIHelper.card("Create Account");
            String[] labels = {"Username", "Password", "Full Name", "Email"};
            JComponent[] fields = {tfUser, tfPass, tfName, tfEmail};

            for (int i = 0; i < 4; i++) {
                JPanel r = UIHelper.row(labels[i], fields[i]);
                r.setAlignmentX(Component.LEFT_ALIGNMENT);
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                card.add(r); card.add(Box.createVerticalStrut(8));
            }

            lblMsg.setFont(UIHelper.F_SMALL); lblMsg.setForeground(UIHelper.DANGER);
            JButton btnSave = UIHelper.btn("Register", UIHelper.ACCENT);
            lblMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            card.add(lblMsg); card.add(Box.createVerticalStrut(6)); card.add(btnSave);

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(16, 16, 16, 16));
            wrap.add(card); add(wrap);

            btnSave.addActionListener(e -> {
                String u = tfUser.getText().trim();
                String p = new String(tfPass.getPassword()).trim();
                String n = tfName.getText().trim();
                String em = tfEmail.getText().trim();
                if (u.length() < 3) { lblMsg.setText("Username min 3 chars."); return; }
                if (p.length() < 6) { lblMsg.setText("Password min 6 chars."); return; }
                if (n.isEmpty())    { lblMsg.setText("Full name required.");    return; }
                try {
                    PreparedStatement ps = DBConnection.get().prepareStatement(
                        "INSERT INTO users (username,password,role,full_name,email) VALUES(?,?,'STUDENT',?,?)");
                    ps.setString(1,u); ps.setString(2,p); ps.setString(3,n); ps.setString(4,em);
                    ps.executeUpdate(); ps.close();
                    JOptionPane.showMessageDialog(this, "Registered! You can now log in.");
                    dispose();
                } catch (SQLException ex) { lblMsg.setText("Error: " + ex.getMessage()); }
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
