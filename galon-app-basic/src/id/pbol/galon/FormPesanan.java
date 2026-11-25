package id.pbol.galon;

import java.awt.Dimension;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FormPesanan extends JDialog {
    private JPanel panelUtama;
    private KolomContoh txtNomorHp;
    private JTextArea txtAlamat;
    private KolomContoh txtJumlah;
    private JLabel lblHargaSatuan;
    private JLabel lblTotal;
    private JButton btnSimpan;
    private JButton btnBatal;

    private final Pengguna konsumen;
    private final PesananDAO pesananDAO = new PesananDAO();
    private boolean tersimpan;

    public FormPesanan(DaftarPesananForm induk, Pengguna konsumen) {
        super(induk, "Galon Air | Buat pesanan", true);
        this.konsumen = konsumen;
        setContentPane(panelUtama);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(470, 455));
        txtNomorHp.setContoh("08xxxxxxxxxx");
        txtJumlah.setContoh("2");
        Tampilan.tombolUtama(btnSimpan);
        getRootPane().setDefaultButton(btnSimpan);

        lblHargaSatuan.setText(Pesanan.formatRupiah(Pesanan.HARGA_PER_GALON) + " per galon");
        txtJumlah.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                tampilkanTotal();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                tampilkanTotal();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                tampilkanTotal();
            }
        });
        btnSimpan.addActionListener(event -> simpan());
        btnBatal.addActionListener(event -> dispose());
        tampilkanTotal();
        pack();
        setLocationRelativeTo(induk);
    }

    public boolean isTersimpan() {
        return tersimpan;
    }

    private int bacaJumlah() {
        try {
            return Integer.parseInt(txtJumlah.getText().strip());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void tampilkanTotal() {
        int jumlah = bacaJumlah();
        if (jumlah > 0) {
            lblTotal.setText(Pesanan.formatRupiah(Pesanan.hitungTotal(jumlah)));
        } else {
            lblTotal.setText("—");
        }
    }

    private void simpan() {
        String nomorHp = txtNomorHp.getText().strip();
        String alamat = txtAlamat.getText().strip();

        if (!nomorHp.matches("(?:08|\\+628)[0-9]{8,11}")) {
            Tampilan.peringatkan(this, txtNomorHp, "Isi nomor HP, misalnya 081234567890 atau +6281234567890.");
            return;
        }
        if (alamat.isEmpty() || alamat.length() > 300) {
            Tampilan.peringatkan(this, txtAlamat, "Isi alamat pengantaran, maksimal 300 karakter.");
            return;
        }

        int jumlah = bacaJumlah();
        if (jumlah <= 0) {
            Tampilan.peringatkan(this, txtJumlah, "Jumlah galon harus berupa bilangan bulat lebih dari nol.");
            return;
        }

        try {
            tersimpan = pesananDAO.buatPesanan(konsumen, nomorHp, alamat, jumlah);
            if (!tersimpan) {
                JOptionPane.showMessageDialog(this, "Pesanan gagal disimpan. Masuk kembali sebagai konsumen.",
                        "Pesanan belum tersimpan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Pesanan berhasil dibuat dan menunggu konfirmasi.",
                    "Pesanan tersimpan", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            Tampilan.galatDatabase(this, e);
        }
    }
}
