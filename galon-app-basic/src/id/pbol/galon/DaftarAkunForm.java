package id.pbol.galon;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class DaftarAkunForm extends JDialog {
    private JPanel panelUtama;
    private JLabel lblJudul;
    private JTextField txtNama;
    private KolomContoh txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtKonfirmasi;
    private JCheckBox chkTampilkan;
    private JButton btnDaftar;
    private JButton btnKembali;

    private final PenggunaDAO penggunaDAO = new PenggunaDAO();
    private final Pengguna admin;
    private String emailTerdaftar;

    public DaftarAkunForm(LoginForm induk) {
        this(induk, null);
    }

    DaftarAkunForm(JFrame induk, Pengguna admin) {
        super(induk, true);
        this.admin = admin;
        setContentPane(panelUtama);
        if (admin != null) {
            lblJudul.setText("Tambah pengantar");
            btnDaftar.setText("Simpan");
        }
        setTitle("Galon Air | " + lblJudul.getText());
        btnKembali.setVisible(admin != null);
        btnKembali.addActionListener(event -> dispose());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Tampilan.fokusSaatDibuka(this, txtEmail);
        chkTampilkan.setOpaque(false);
        txtEmail.setContoh("nama@email.com");
        Tampilan.aturKataSandi(chkTampilkan, txtPassword, txtKonfirmasi);
        Tampilan.tombolUtama(btnDaftar);
        btnDaftar.addActionListener(event -> daftar());
        getRootPane().setDefaultButton(btnDaftar);
        pack();
        setLocationRelativeTo(induk);
    }

    public String getEmailTerdaftar() {
        return emailTerdaftar;
    }

    private void daftar() {
        String nama = txtNama.getText().strip();
        String email = txtEmail.getText().strip().toLowerCase(Locale.ROOT);
        if (nama.isEmpty() || nama.length() > 100) {
            Tampilan.peringatkan(this, txtNama, "Isi nama, maksimal 100 karakter.");
            return;
        }
        if (!Tampilan.periksaEmail(this, txtEmail, email)) {
            return;
        }
        char[] password = txtPassword.getPassword();
        char[] konfirmasi = txtKonfirmasi.getPassword();
        try {
            if (password.length == 0) {
                Tampilan.peringatkan(this, txtPassword, "Isi kata sandi.");
                return;
            }
            if (!Arrays.equals(password, konfirmasi)) {
                Tampilan.peringatkan(this, txtKonfirmasi, "Ulangi kata sandi yang sama di kolom konfirmasi.");
                return;
            }
            boolean berhasil;
            String pesan;
            if (admin == null) {
                berhasil = penggunaDAO.daftarKonsumen(nama, email, password);
                pesan = "Akun berhasil dibuat. Silakan masuk.";
            } else {
                berhasil = penggunaDAO.tambahPengantar(admin, nama, email, password);
                pesan = "Akun pengantar berhasil ditambahkan";
            }
            if (!berhasil) {
                Tampilan.peringatkan(this, txtEmail, "Email sudah terdaftar");
                return;
            }
            emailTerdaftar = email;
            JOptionPane.showMessageDialog(this, pesan, lblJudul.getText(), JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Akses ditolak", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            Tampilan.galatDatabase(this, e);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(konfirmasi, '\0');
        }
    }
}
