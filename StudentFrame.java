import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class StudentFrame extends JFrame {

    private final int userId;
    private final String fullName;

    public StudentFrame(int userId, String fullName) {
        this.userId = userId;
        this.fullName = fullName;

        setTitle("Student Portal ");
        setSize(720, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        getContentPane().setBackground(UIHelper.BG);

        JButton btnLogout = UIHelper.btn("Logout", UIHelper.DANGER);
        add(UIHelper.header("Student Panel ", btnLogout), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.F_BOLD);

        tabs.addTab("Available Exams", examsTab());
        tabs.addTab("My Results", resultsTab());

        add(tabs, BorderLayout.CENTER);

        btnLogout.addActionListener(e -> {
            dispose();
            DBConnection.close();
            new LoginFrame().setVisible(true);
        });
    }

    // exams tab
    private JPanel examsTab() {

        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UIHelper.BG);
        p.setBorder(new EmptyBorder(12,12,12,12));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID","Exam Title","Duration","Marks","Status"}, 0);

        JTable table = new JTable(model);

        JButton btnStart   = UIHelper.btn("Start Exam", UIHelper.ACCENT);
        JButton btnRefresh = UIHelper.btn("Refresh", UIHelper.PRIMARY);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.setOpaque(false);
        btns.add(btnStart);
        btns.add(btnRefresh);

        p.add(UIHelper.table(table), BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);

        // LOAD EXAMS
        Runnable load = () -> {
            model.setRowCount(0);

            try {
                ResultSet rs = DBConnection.get().createStatement()
                        .executeQuery("SELECT * FROM exams WHERE is_active=1");

                while (rs.next()) {
                    boolean done = attempted(rs.getInt("id"));

                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getInt("duration_mins") + " min",
                            rs.getInt("total_marks"),
                            done ? "Completed" : "Available"
                    });
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(p, ex.getMessage());
            }
        };

        load.run();
        btnRefresh.addActionListener(e -> load.run());

        // START EXAM
        btnStart.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row < 0) {
                JOptionPane.showMessageDialog(p, "Select an exam");
                return;
            }

            if ("Completed".equals(model.getValueAt(row, 4))) {
                JOptionPane.showMessageDialog(p, "Already attempted");
                return;
            }

            int examId = (int) model.getValueAt(row, 0);

            new ExamFrame(userId, examId, this, load).setVisible(true);
        });

        return p;
    }

    // result
    private JPanel resultsTab() {

        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UIHelper.BG);
        p.setBorder(new EmptyBorder(12,12,12,12));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Exam","Score","Total","%","Status","Date"}, 0);

        JTable table = new JTable(model);

        JButton btnRefresh = UIHelper.btn("Refresh", UIHelper.PRIMARY);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(btnRefresh);

        p.add(top, BorderLayout.NORTH);
        p.add(UIHelper.table(table), BorderLayout.CENTER);

        Runnable load = () -> {
            model.setRowCount(0);

            try {
                PreparedStatement ps = DBConnection.get().prepareStatement(
                        "SELECT e.title,r.score,r.total_marks,r.percentage,r.status,r.attempted_at " +
                                "FROM results r JOIN exams e ON e.id=r.exam_id WHERE r.user_id=?");

                ps.setInt(1, userId);

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("title"),
                            rs.getInt("score"),
                            rs.getInt("total_marks"),
                            String.format("%.1f%%", rs.getDouble("percentage")),
                            rs.getString("status"),
                            rs.getTimestamp("attempted_at")
                    });
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(p, ex.getMessage());
            }
        };

        load.run();
        btnRefresh.addActionListener(e -> load.run());

        return p;
    }

    //CHECK ATTEMPT
    private boolean attempted(int examId) {
        try {
            PreparedStatement ps = DBConnection.get().prepareStatement(
                    "SELECT COUNT(*) FROM results WHERE user_id=? AND exam_id=?");

            ps.setInt(1, userId);
            ps.setInt(2, examId);

            ResultSet rs = ps.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            return false;
        }
    }
}
