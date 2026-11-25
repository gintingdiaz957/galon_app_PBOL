package id.pbol.galon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.JToggleButton;
import javax.swing.plaf.FontUIResource;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;

final class Tampilan {
    private Tampilan() {
    }

    static final Color BIRU = new Color(30, 82, 167);
    private static final Color TEKS = new Color(30, 44, 63);

    static void aturGaya() {
        Font font = (Font) Toolkit.getDefaultToolkit().getDesktopProperty("win.messagebox.font");
        if (font == null) {
            font = UIManager.getFont("Label.font");
        }
        FontUIResource normal = new FontUIResource(font.deriveFont(Font.PLAIN, Math.max(14f, font.getSize2D())));
        for (String jenis : new String[]{"Label", "Button", "ToggleButton", "CheckBox", "TextField",
                "PasswordField", "TextArea", "Table", "TableHeader", "ComboBox", "OptionPane"}) {
            UIManager.put(jenis + ".font", normal);
            UIManager.put(jenis + ".foreground", TEKS);
        }
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("ToggleButton.background", Color.WHITE);
        UIManager.put("Button.disabledText", new Color(88, 99, 113));
        UIManager.put("Button.margin", new Insets(6, 14, 6, 14));
        UIManager.put("ToggleButton.margin", new Insets(6, 14, 6, 14));
        UIManager.put("ToggleButton.select", BIRU);
        UIManager.put("TextField.margin", new Insets(6, 8, 6, 8));
        UIManager.put("PasswordField.margin", new Insets(6, 8, 6, 8));
        UIManager.put("TextArea.margin", new Insets(6, 8, 6, 8));
        UIManager.put("Table.selectionBackground", new Color(219, 234, 254));
        UIManager.put("Table.selectionForeground", TEKS);
    }

    static void fokusSaatDibuka(Window jendela, Component kolom) {
        jendela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                kolom.requestFocusInWindow();
            }
        });
    }

    static void tombolUtama(JButton tombol) {
        tombol.setBackground(BIRU);
        tombol.setForeground(Color.WHITE);
        tombol.addPropertyChangeListener("enabled", event -> tombol.setBackground(
                tombol.isEnabled() ? BIRU : new Color(229, 234, 240)));
    }

    static void tabDaftar(JToggleButton tab) {
        Font normal = tab.getFont();
        tab.addItemListener(event -> {
            tab.setFont(normal.deriveFont(tab.isSelected() ? Font.BOLD : Font.PLAIN));
            tab.setForeground(tab.isSelected() ? Color.WHITE : TEKS);
            tab.setBackground(tab.isSelected() ? BIRU : Color.WHITE);
        });
    }

    static void tombolPembatalan(JButton tombol) {
        tombol.setBackground(Color.WHITE);
        tombol.setForeground(new Color(161, 39, 39));
    }

    static boolean konfirmasi(Component induk, String pesan) {
        String[] pilihan = {"Ya", "Tidak"};
        return JOptionPane.showOptionDialog(induk, pesan, "Konfirmasi",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, pilihan, pilihan[1]) == 0;
    }

    static void konfirmasiTutup(JFrame jendela) {
        jendela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jendela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (konfirmasi(jendela, "Tutup aplikasi?")) {
                    jendela.dispose();
                }
            }
        });
    }

    static void aturKataSandi(JCheckBox pilihan, JPasswordField... kolomSandi) {
        for (JPasswordField kolom : kolomSandi) {
            char penutup = kolom.getEchoChar();
            pilihan.addActionListener(event -> kolom.setEchoChar(pilihan.isSelected() ? (char) 0 : penutup));
        }
    }

    static void peringatkan(Component induk, Component kolom, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Periksa isian", JOptionPane.WARNING_MESSAGE);
        kolom.requestFocusInWindow();
    }

    static boolean periksaEmail(Component induk, JTextField kolom, String email) {
        if (email.length() > 150 || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            peringatkan(induk, kolom, "Isi email yang benar, misalnya nama@email.com (maksimal 150 karakter).");
            return false;
        }
        return true;
    }

    static void galatDatabase(Component induk, SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(induk,
                "Data gagal diakses. Periksa koneksi dan pengaturan MySQL.",
                "Galat database", JOptionPane.ERROR_MESSAGE);
    }
}
