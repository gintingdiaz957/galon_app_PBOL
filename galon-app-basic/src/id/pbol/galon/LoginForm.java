package id.pbol.galon;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;

public class LoginForm extends JFrame {
    private JPanel panelUtama;
    private KolomContoh txtEmail;
    private JCheckBox chkTampilkan;
    private JButton btnDaftar;
    private JPasswordField txtPassword;
    private JButton btnMasuk;

    private final PenggunaDAO penggunaDAO = new PenggunaDAO();

    public LoginForm() {
        super("Galon Air | Masuk");
        setContentPane(panelUtama);
        Tampilan.konfirmasiTutup(this);
        Tampilan.fokusSaatDibuka(this, txtEmail);
        chkTampilkan.setOpaque(false);
        txtEmail.setContoh("nama@email.com");
        Tampilan.aturKataSandi(chkTampilkan, txtPassword);
        btnDaftar.addActionListener(event -> bukaPendaftaran());
        setMinimumSize(new Dimension(400, 340));
        Tampilan.tombolUtama(btnMasuk);
        getRootPane().setDefaultButton(btnMasuk);
        btnMasuk.addActionListener(event -> masuk());
        pack();
        setLocationRelativeTo(null);
    }

    private void bukaPendaftaran() {
        DaftarAkunForm form = new DaftarAkunForm(this);
        form.setVisible(true);
        if (form.getEmailTerdaftar() != null) {
            txtEmail.setText(form.getEmailTerdaftar());
            txtPassword.setText("");
            if (chkTampilkan.isSelected()) {
                chkTampilkan.doClick();
            }
            txtPassword.requestFocusInWindow();
        }
    }

    private void masuk() {
        String email = txtEmail.getText().strip().toLowerCase(Locale.ROOT);
        char[] password = txtPassword.getPassword();

        try {
            if (!Tampilan.periksaEmail(this, txtEmail, email)) {
                return;
            }
            if (password.length == 0) {
                Tampilan.peringatkan(this, txtPassword, "Isi kata sandi.");
                return;
            }

            Pengguna pengguna = penggunaDAO.login(email, password);
            if (pengguna == null) {
                JOptionPane.showMessageDialog(this, "Email atau kata sandi salah.",
                        "Login gagal", JOptionPane.WARNING_MESSAGE);
                txtPassword.requestFocusInWindow();
                return;
            }

            new DaftarPesananForm(pengguna).setVisible(true);
            dispose();
        } catch (SQLException e) {
            Tampilan.galatDatabase(this, e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
