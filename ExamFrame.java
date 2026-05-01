import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExamFrame extends JFrame {

    private final int userId;
    private final Runnable onDone;

    private final java.util.List<int[]> qdata = new ArrayList<>();
    private final java.util.List<String[]> qtxt = new ArrayList<>();
    private final Map<Integer, String> answers = new HashMap<>();

    private int pointer = 0, totalMarks, passingMarks;

    // UI components
    private final JLabel lblProgress = new JLabel();
    private final JLabel lblQ        = new JLabel();

    private final JRadioButton[] radios = new JRadioButton[4];
    private final ButtonGroup group = new ButtonGroup();

    private final JButton btnPrev   = UIHelper.btn("← Prev",  new Color(100,100,120));
    private final JButton btnNext   = UIHelper.btn("Next →",  UIHelper.PRIMARY);
    private final JButton btnSubmit = UIHelper.btn("Submit ", UIHelper.ACCENT);

    public ExamFrame(int userId, int examId, JFrame parent, Runnable onDone) {
        this.userId = userId;
        this.onDone = onDone;

        setTitle("Exam in Progress");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(640, 400);
        setResizable(false);
        setLocationRelativeTo(parent);

        buildUI();
        load(examId);
    }

    private void buildUI() {

        getContentPane().setBackground(UIHelper.WHITE);
        setLayout(new BorderLayout());

        // TOP BAR
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIHelper.PRIMARY);
        top.setBorder(new EmptyBorder(8,16,8,16));

        lblProgress.setFont(UIHelper.F_BOLD);
        lblProgress.setForeground(UIHelper.WHITE);

        top.add(lblProgress, BorderLayout.WEST);

        // QUESTION
        lblQ.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblQ.setForeground(UIHelper.TEXT);
        lblQ.setVerticalAlignment(SwingConstants.TOP);
        lblQ.setBorder(new EmptyBorder(0,0,12,0));

        JPanel opts = new JPanel(new GridLayout(4,1,0,6));
        opts.setOpaque(false);

        String[] letters = {"A","B","C","D"};

        for (int i = 0; i < 4; i++) {
            radios[i] = new JRadioButton();
            radios[i].setFont(UIHelper.F_LABEL);
            radios[i].setForeground(UIHelper.TEXT);
            radios[i].setOpaque(false);
            radios[i].setActionCommand(letters[i]);
            group.add(radios[i]);
            opts.add(radios[i]);
        }

        JPanel center = new JPanel(new BorderLayout(0,8));
        center.setBackground(UIHelper.WHITE);
        center.setBorder(new EmptyBorder(16,24,8,24));
        center.add(lblQ, BorderLayout.NORTH);
        center.add(opts, BorderLayout.CENTER);

        // NAVIGATION
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER,10,8));
        nav.setBackground(new Color(240,242,246));
        nav.setBorder(new MatteBorder(1,0,0,0, UIHelper.BORDER));

        nav.add(btnPrev);
        nav.add(btnNext);
        nav.add(btnSubmit);

        add(top,    BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(nav,    BorderLayout.SOUTH);

        // BUTTON ACTIONS
        btnPrev.addActionListener(e -> {
            save();
            if (pointer > 0) {
                pointer--;
                showQuestion();
            }
        });

        btnNext.addActionListener(e -> {
            save();
            if (pointer < qdata.size()-1) {
                pointer++;
                showQuestion();
            }
        });

        btnSubmit.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Submit now?", "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                save();
                finish();
            }
        });
    }

    private void load(int examId) {
        try {
            PreparedStatement ep = DBConnection.get().prepareStatement(
                    "SELECT * FROM exams WHERE id=?");

            ep.setInt(1, examId);
            ResultSet er = ep.executeQuery();

            if (!er.next()) {
                JOptionPane.showMessageDialog(this, "Exam not found");
                dispose();
                return;
            }

            totalMarks   = er.getInt("total_marks");
            passingMarks = er.getInt("passing_marks");

            er.close(); ep.close();

            PreparedStatement qp = DBConnection.get().prepareStatement(
                    "SELECT * FROM questions WHERE exam_id=? ORDER BY id");

            qp.setInt(1, examId);
            ResultSet qr = qp.executeQuery();

            while (qr.next()) {
                qdata.add(new int[]{
                        qr.getInt("id"),
                        qr.getInt("marks"),
                        examId
                });

                qtxt.add(new String[]{
                        qr.getString("question_text"),
                        qr.getString("option_a"),
                        qr.getString("option_b"),
                        qr.getString("option_c"),
                        qr.getString("option_d"),
                        qr.getString("correct_answer")
                });
            }

            qr.close(); qp.close();

            if (qdata.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No questions.");
                dispose();
                return;
            }

            showQuestion();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            dispose();
        }
    }

    private void showQuestion() {

        String[] q = qtxt.get(pointer);

        lblProgress.setText("Question " + (pointer+1) + " of " + qdata.size());

        lblQ.setText("<html><body style='width:370px'>" + q[0] + "</body></html>");

        group.clearSelection();

        for (int i = 0; i < 4; i++) {
            radios[i].setText(
                    new String[]{"A","B","C","D"}[i] + ") " + q[i+1]
            );
        }

        String saved = answers.get(qdata.get(pointer)[0]);

        if (saved != null) {
            for (JRadioButton r : radios) {
                if (r.getActionCommand().equals(saved)) {
                    r.setSelected(true);
                }
            }
        }

        btnPrev.setEnabled(pointer > 0);
        btnNext.setEnabled(pointer < qdata.size()-1);
    }

    private void save() {
        ButtonModel sel = group.getSelection();

        if (sel != null) {
            answers.put(qdata.get(pointer)[0], sel.getActionCommand());
        }
    }

    private void finish() {

        save();

        int score = 0;

        for (int i = 0; i < qdata.size(); i++) {
            String given = answers.get(qdata.get(i)[0]);

            if (given != null && given.equals(qtxt.get(i)[5])) {
                score += qdata.get(i)[1];
            }
        }

        double pct = totalMarks > 0 ? score * 100.0 / totalMarks : 0;
        String status = score >= passingMarks ? "PASS" : "FAIL";

        try {
            PreparedStatement ps = DBConnection.get().prepareStatement(
                    "INSERT INTO results(user_id,exam_id,score,total_marks,percentage,status) VALUES(?,?,?,?,?,?)");

            ps.setInt(1, userId);
            ps.setInt(2, qdata.get(0)[2]);
            ps.setInt(3, score);
            ps.setInt(4, totalMarks);
            ps.setDouble(5, pct);
            ps.setString(6, status);

            ps.executeUpdate();
            ps.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage());
        }

        JOptionPane.showMessageDialog(this,
                String.format("Exam Complete!\n\nScore: %d/%d\nPercent: %.1f%%\nStatus: %s",
                        score, totalMarks, pct, status));

        dispose();

        if (onDone != null) onDone.run();
    }
}
