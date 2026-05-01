import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class UIHelper {
    static final Color BG      = new Color(245, 247, 250);
    static final Color PRIMARY = new Color(52,  73, 130);
    static final Color ACCENT  = new Color(76, 175,  80);
    static final Color DANGER  = new Color(211,  47,  47);
    static final Color WHITE   = Color.WHITE;
    static final Color BORDER  = new Color(210, 215, 225);
    static final Color TEXT    = new Color(33,  37,  41);
    static final Color SUBTEXT = new Color(100, 110, 120);

    static final Font F_TITLE = new Font("SansSerif", Font.BOLD,  20);
    static final Font F_LABEL = new Font("SansSerif", Font.PLAIN, 13);
    static final Font F_BOLD  = new Font("SansSerif", Font.BOLD,  13);
    static final Font F_SMALL = new Font("SansSerif", Font.PLAIN, 11);

    // button
    static JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(WHITE);
        b.setFont(F_BOLD); b.setFocusPainted(false);
        b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Label + field row
    static JPanel row(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(F_LABEL); l.setForeground(SUBTEXT);
        l.setPreferredSize(new Dimension(110, 26));
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    // White panel
    static JPanel card(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(20, 24, 20, 24)));
        if (title != null && !title.isEmpty()) {
            JLabel t = new JLabel(title);
            t.setFont(F_TITLE); t.setForeground(PRIMARY);
            t.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(t); p.add(Box.createVerticalStrut(16));
        }
        return p;
    }

    // Styled JTable
    static JScrollPane table(JTable tbl) {
        tbl.setFont(F_LABEL); tbl.setRowHeight(26);
        tbl.setGridColor(BORDER);
        tbl.setSelectionBackground(new Color(224, 231, 255));
        tbl.setFillsViewportHeight(true);
        tbl.setDefaultEditor(Object.class, null);
        tbl.getTableHeader().setFont(F_BOLD);
        tbl.getTableHeader().setBackground(PRIMARY);
        tbl.getTableHeader().setForeground(WHITE);
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(new LineBorder(BORDER));
        return sp;
    }

    // Top header bar used in AdminStudent frames
    static JPanel header(String text, JButton action) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(PRIMARY); h.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel l = new JLabel(text);
        l.setFont(F_TITLE); l.setForeground(WHITE);
        action.setPreferredSize(new Dimension(80, 30));
        h.add(l, BorderLayout.WEST); h.add(action, BorderLayout.EAST);
        return h;
    }

    
    static JTextField field() {
        JTextField tf = new JTextField();
        tf.setFont(F_LABEL); tf.setBorder(new LineBorder(BORDER, 1));
        return tf;
    }

    // Password field
    static JPasswordField passField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(F_LABEL); pf.setBorder(new LineBorder(BORDER, 1));
        return pf;
    }
}
