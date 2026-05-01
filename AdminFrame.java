import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AdminFrame extends JFrame {

    public AdminFrame(String name) {
        setTitle("Admin — " + name);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 560); setLocationRelativeTo(null);
        getContentPane().setBackground(UIHelper.BG);

        JButton btnLogout = UIHelper.btn("Logout", UIHelper.DANGER);
        add(UIHelper.header("Admin Panel  ·  " + name, btnLogout), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.F_BOLD);
        tabs.addTab("Exams",     examsTab());
        tabs.addTab("Questions", questionsTab());
        tabs.addTab("Results",   resultsTab());
        add(tabs, BorderLayout.CENTER);

        btnLogout.addActionListener(e -> {
            dispose(); DBConnection.close();
            new LoginFrame().setVisible(true);
        });
    }

    // EXAMS TAB 
    private JPanel examsTab() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UIHelper.BG); p.setBorder(new EmptyBorder(12,12,12,12));

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Title","Duration (min)","Total Marks","Pass Marks"}, 0);
        JTable table = new JTable(model);

        JTextField tfTitle = UIHelper.field(), tfDur = UIHelper.field(), tfPass = UIHelper.field();
        JPanel form = new JPanel(new GridLayout(1, 6, 6, 0));
        form.setOpaque(false);
        form.add(new JLabel("Title:")); form.add(tfTitle);
        form.add(new JLabel("Duration:")); form.add(tfDur);
        form.add(new JLabel("Pass Marks:")); form.add(tfPass);

        JButton btnAdd = UIHelper.btn("Add", UIHelper.ACCENT);
        JButton btnDel = UIHelper.btn("Delete", UIHelper.DANGER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false); btns.add(btnAdd); btns.add(btnDel);

        JPanel bottom = new JPanel(new BorderLayout(0,6));
        bottom.setOpaque(false); bottom.add(form, BorderLayout.CENTER); bottom.add(btns, BorderLayout.SOUTH);
        p.add(UIHelper.table(table), BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);

        Runnable load = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = DBConnection.get().createStatement()
                    .executeQuery("SELECT * FROM exams ORDER BY id");
                while (rs.next()) model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("title"),
                    rs.getInt("duration_mins"), rs.getInt("total_marks"), rs.getInt("passing_marks")});
            } catch (SQLException ex) { JOptionPane.showMessageDialog(p, ex.getMessage()); }
        };
        load.run();

        btnAdd.addActionListener(e -> {
            try {
                if (tfTitle.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(p,"Title required"); return; }
                PreparedStatement ps = DBConnection.get().prepareStatement(
                    "INSERT INTO exams (title,duration_mins,passing_marks) VALUES(?,?,?)");
                ps.setString(1, tfTitle.getText().trim());
                ps.setInt(2, Integer.parseInt(tfDur.getText().trim()));
                ps.setInt(3, Integer.parseInt(tfPass.getText().trim()));
                ps.executeUpdate(); ps.close();
                tfTitle.setText(""); tfDur.setText(""); tfPass.setText("");
                load.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, "Error: " + ex.getMessage()); }
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(p, "Select a row."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(p, "Delete exam ID " + id + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try {
                PreparedStatement ps = DBConnection.get().prepareStatement("DELETE FROM exams WHERE id=?");
                ps.setInt(1, id); ps.executeUpdate(); ps.close(); load.run();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(p, ex.getMessage()); }
        });
        return p;
    }

    //  QUESTIONS TAB
    private JPanel questionsTab() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UIHelper.BG); p.setBorder(new EmptyBorder(12,12,12,12));

        JComboBox<String> cbExam = new JComboBox<>(); cbExam.setFont(UIHelper.F_LABEL);
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        topBar.setOpaque(false); topBar.add(new JLabel("Exam:")); topBar.add(cbExam);

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Question","A","B","C","D","Correct","Marks"}, 0);
        JTable table = new JTable(model);

        JTextField tfQ = UIHelper.field(), tfA = UIHelper.field(), tfB = UIHelper.field();
        JTextField tfC = UIHelper.field(), tfD = UIHelper.field(), tfM = UIHelper.field();
        tfM.setText("1");
        JComboBox<String> cbAns = new JComboBox<>(new String[]{"A","B","C","D"});
        cbAns.setFont(UIHelper.F_LABEL);

        JPanel form = new JPanel(new GridLayout(2, 8, 5, 5));
        form.setOpaque(false);
        for (Object[] pair : new Object[][]{
            {"Question:", tfQ}, {"A:", tfA}, {"B:", tfB}, {"C:", tfC},
            {"D:", tfD}, {"Correct:", cbAns}, {"Marks:", tfM}}) {
            form.add(new JLabel((String) pair[0]));
            form.add((Component) pair[1]);
        }

        JButton btnAdd = UIHelper.btn("Add Question", UIHelper.ACCENT);
        JButton btnDel = UIHelper.btn("Delete", UIHelper.DANGER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
        btns.setOpaque(false); btns.add(btnAdd); btns.add(btnDel);

        JPanel bottom = new JPanel(new BorderLayout(0,6));
        bottom.setOpaque(false); bottom.add(form, BorderLayout.CENTER); bottom.add(btns, BorderLayout.SOUTH);
        p.add(topBar, BorderLayout.NORTH);
        p.add(UIHelper.table(table), BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);

        Runnable loadExams = () -> {
            cbExam.removeAllItems();
            try {
                ResultSet rs = DBConnection.get().createStatement()
                    .executeQuery("SELECT id,title FROM exams ORDER BY id");
                while (rs.next()) cbExam.addItem(rs.getInt("id") + " — " + rs.getString("title"));
            } catch (SQLException ex) {}
        };
        loadExams.run();

        Runnable loadQ = () -> {
            model.setRowCount(0);
            String sel = (String) cbExam.getSelectedItem();
            if (sel == null) return;
            int eid = Integer.parseInt(sel.split(" — ")[0]);
            try {
                PreparedStatement ps = DBConnection.get().prepareStatement(
                    "SELECT * FROM questions WHERE exam_id=? ORDER BY id");
                ps.setInt(1, eid);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String q = rs.getString("question_text");
                    if (q.length() > 35) q = q.substring(0,35) + "..";
                    model.addRow(new Object[]{rs.getInt("id"), q,
                        rs.getString("option_a"), rs.getString("option_b"),
                        rs.getString("option_c"), rs.getString("option_d"),
                        rs.getString("correct_answer"), rs.getInt("marks")});
                }
                rs.close(); ps.close();
            } catch (SQLException ex) {}
        };
        cbExam.addActionListener(e -> loadQ.run());
        loadQ.run();

        btnAdd.addActionListener(e -> {
            String sel = (String) cbExam.getSelectedItem();
            if (sel == null) { JOptionPane.showMessageDialog(p,"Select an exam."); return; }
            int eid = Integer.parseInt(sel.split(" — ")[0]);
            try {
                PreparedStatement ps = DBConnection.get().prepareStatement(
                    "INSERT INTO questions(exam_id,question_text,option_a,option_b,option_c,option_d,correct_answer,marks) VALUES(?,?,?,?,?,?,?,?)");
                ps.setInt(1,eid); ps.setString(2,tfQ.getText().trim());
                ps.setString(3,tfA.getText().trim()); ps.setString(4,tfB.getText().trim());
                ps.setString(5,tfC.getText().trim()); ps.setString(6,tfD.getText().trim());
                ps.setString(7,(String)cbAns.getSelectedItem());
                ps.setInt(8, Integer.parseInt(tfM.getText().trim()));
                ps.executeUpdate(); ps.close();
                DBConnection.get().prepareStatement(
                    "UPDATE exams SET total_marks=(SELECT COALESCE(SUM(marks),0) FROM questions WHERE exam_id="+eid+") WHERE id="+eid)
                    .executeUpdate();
                for (JTextField tf : new JTextField[]{tfQ,tfA,tfB,tfC,tfD}) tf.setText("");
                tfM.setText("1"); loadQ.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(p,"Error: "+ex.getMessage()); }
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(p,"Select a question."); return; }
            int qid = (int) model.getValueAt(row,0);
            String sel = (String) cbExam.getSelectedItem();
            int eid = Integer.parseInt(sel.split(" — ")[0]);
            try {
                DBConnection.get().prepareStatement("DELETE FROM questions WHERE id="+qid).executeUpdate();
                DBConnection.get().prepareStatement(
                    "UPDATE exams SET total_marks=(SELECT COALESCE(SUM(marks),0) FROM questions WHERE exam_id="+eid+") WHERE id="+eid)
                    .executeUpdate();
                loadQ.run();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(p, ex.getMessage()); }
        });
        return p;
    }

    // RESULTS TAB
    private JPanel resultsTab() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(UIHelper.BG); p.setBorder(new EmptyBorder(12,12,12,12));

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Student","Exam","Score","Total","%","Status","Date"}, 0);
        JTable table = new JTable(model);

        JButton btnRefresh = UIHelper.btn("Refresh", UIHelper.PRIMARY);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false); top.add(btnRefresh);
        p.add(top, BorderLayout.NORTH);
        p.add(UIHelper.table(table), BorderLayout.CENTER);

        Runnable load = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = DBConnection.get().createStatement().executeQuery(
                    "SELECT r.id, u.username, e.title, r.score, r.total_marks, " +
                    "r.percentage, r.status, r.attempted_at FROM results r " +
                    "JOIN users u ON u.id=r.user_id JOIN exams e ON e.id=r.exam_id " +
                    "ORDER BY r.attempted_at DESC");
                while (rs.next()) model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("username"), rs.getString("title"),
                    rs.getInt("score"), rs.getInt("total_marks"),
                    String.format("%.1f%%", rs.getDouble("percentage")),
                    rs.getString("status"), rs.getTimestamp("attempted_at")});
            } catch (SQLException ex) { JOptionPane.showMessageDialog(p, ex.getMessage()); }
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        return p;
    }
}
