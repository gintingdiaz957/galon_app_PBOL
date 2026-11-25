package id.pbol.galon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DaftarPesananForm extends JFrame {
    private JPanel panelUtama;
    private JLabel lblAkun;
    private JLabel lblMenunggu;
    private JLabel lblInformasi;
    private JLabel lblPengantar;
    private JTable tabelPesanan;
    private JComboBox<Pengguna> cmbPengantar;
    private JToggleButton btnAktif;
    private JToggleButton btnRiwayat;
    private JButton btnMuatUlang;
    private JButton btnKeluar;
    private JButton btnBuatPesanan;
    private JButton btnTerimaTugaskan;
    private JButton btnTambahPengantar;
    private JButton btnMulaiAntar;
    private JButton btnSelesaikan;
    private JButton btnBatalkan;

    private final Pengguna pengguna;
    private final PenggunaDAO penggunaDAO = new PenggunaDAO();
    private final PesananDAO pesananDAO = new PesananDAO();
    private final DefaultTableModel modelTabel = new DefaultTableModel(
            new String[]{"Status", "Penerima", "Jumlah", "Alamat", "Total", "Pengantar",
                    "No. HP", "Waktu", "ID", "Alasan pembatalan"}, 0) {
        @Override
        public boolean isCellEditable(int baris, int kolom) {
            return false;
        }
    };

    private List<Pesanan> daftarPesanan = List.of();

    public DaftarPesananForm(Pengguna pengguna) {
        super("Galon Air | Daftar pesanan");
        this.pengguna = pengguna;
        setContentPane(panelUtama);
        Tampilan.konfirmasiTutup(this);
        setMinimumSize(new Dimension(750, 480));
        lblAkun.setText(pengguna + "  ·  " + pengguna.getPeran());

        siapkanTabel();
        aturPeran();
        ButtonGroup pilihanDaftar = new ButtonGroup();
        pilihanDaftar.add(btnAktif);
        pilihanDaftar.add(btnRiwayat);
        Tampilan.tabDaftar(btnAktif);
        Tampilan.tabDaftar(btnRiwayat);
        btnAktif.setSelected(true);
        btnRiwayat.addItemListener(event -> muatTabTerpilih());
        btnMuatUlang.addActionListener(event -> muatUlang(true));
        btnKeluar.addActionListener(event -> keluar());
        btnBatalkan.addActionListener(event -> batalkan());
        btnBuatPesanan.addActionListener(event -> buatPesanan());
        btnTambahPengantar.addActionListener(event -> tambahPengantar());
        btnTerimaTugaskan.addActionListener(event -> terimaDanTugaskan());
        btnMulaiAntar.addActionListener(event -> mulaiAntar());
        btnSelesaikan.addActionListener(event -> selesaikanPesanan());
        tabelPesanan.getSelectionModel().addListSelectionListener(event -> aturTombolPesanan());
        cmbPengantar.addActionListener(event -> aturTombolPesanan());

        muatUlang(true);
        if (pengguna.getPeran() == Pengguna.Peran.ADMIN) {
            Timer timerAdmin = new Timer(5_000, event -> muatUlang(false));
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent event) {
                    timerAdmin.stop();
                }
            });
            timerAdmin.start();
        }
        pack();
        setSize(Math.max(getWidth(), 1_140), Math.max(getHeight(), 590));
        setLocationRelativeTo(null);
    }

    private void siapkanTabel() {
        tabelPesanan.setModel(modelTabel);
        tabelPesanan.setRowHeight(Math.max(34, tabelPesanan.getFontMetrics(tabelPesanan.getFont()).getHeight() + 12));
        tabelPesanan.setShowVerticalLines(false);
        tabelPesanan.setGridColor(new java.awt.Color(219, 226, 234));
        tabelPesanan.setFillsViewportHeight(true);
        tabelPesanan.getTableHeader().setFont(tabelPesanan.getFont().deriveFont(java.awt.Font.BOLD));
        tabelPesanan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelPesanan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabelPesanan.getTableHeader().setReorderingAllowed(false);
        int[] lebarKolom = {180, 160, 65, 290, 105, 145, 140, 155, 60, 220};
        for (int kolom = 0; kolom < lebarKolom.length; kolom++) {
            tabelPesanan.getColumnModel().getColumn(kolom).setPreferredWidth(lebarKolom[kolom]);
        }
        DefaultTableCellRenderer teksPanjang = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabel, Object nilai,
                    boolean dipilih, boolean fokus, int baris, int kolom) {
                putClientProperty("html.disable", true);
                super.getTableCellRendererComponent(tabel, nilai, dipilih, fokus, baris, kolom);
                String teks = String.valueOf(nilai)
                        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                        .replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br>");
                setToolTipText("<html><body style='width:260px'>" + teks + "</body></html>");
                return this;
            }
        };
        tabelPesanan.getColumn("Alamat").setCellRenderer(teksPanjang);
        tabelPesanan.getColumn("Alasan pembatalan").setCellRenderer(teksPanjang);
    }

    private void aturPeran() {
        boolean konsumen = pengguna.getPeran() == Pengguna.Peran.KONSUMEN;
        boolean admin = pengguna.getPeran() == Pengguna.Peran.ADMIN;
        boolean pengantar = pengguna.getPeran() == Pengguna.Peran.PENGANTAR;

        lblMenunggu.setVisible(admin);
        lblPengantar.setVisible(admin);
        cmbPengantar.setVisible(admin);
        btnTambahPengantar.setVisible(admin);
        btnBuatPesanan.setVisible(konsumen);
        btnTerimaTugaskan.setVisible(admin);
        btnBatalkan.setVisible(admin || pengantar);
        btnMulaiAntar.setVisible(pengantar);
        btnSelesaikan.setVisible(pengantar);

        Tampilan.tombolPembatalan(btnBatalkan);
        Tampilan.tombolUtama(btnBuatPesanan);
        Tampilan.tombolUtama(btnTerimaTugaskan);
        Tampilan.tombolUtama(btnMulaiAntar);
        Tampilan.tombolUtama(btnSelesaikan);
    }

    private void muatTabTerpilih() {
        tabelPesanan.clearSelection();
        muatUlang(true);
    }

    private void muatUlang(boolean manual) {
        Pesanan terpilih = pesananTerpilih();
        try {
            List<Pesanan> daftarBaru = pesananDAO.daftarPesanan(pengguna, btnRiwayat.isSelected());
            if (pengguna.getPeran() == Pengguna.Peran.ADMIN) {
                long menunggu = pesananDAO.jumlahMenunggu(pengguna);
                lblMenunggu.setText("Menunggu konfirmasi: " + menunggu);
                if (manual) {
                    isiPengantar();
                }
            }

            daftarPesanan = daftarBaru;
            isiTabel(terpilih);
            tampilkanInformasiDaftar();
        } catch (SQLException e) {
            daftarPesanan = List.of();
            modelTabel.setRowCount(0);
            lblMenunggu.setText("Menunggu konfirmasi: —");
            lblInformasi.setText("Daftar gagal dimuat. Tekan Muat ulang untuk mencoba lagi.");
            if (manual) {
                Tampilan.galatDatabase(this, e);
            }
        }
        aturTombolPesanan();
    }

    private void tampilkanInformasiDaftar() {
        boolean riwayat = btnRiwayat.isSelected();
        if (!daftarPesanan.isEmpty()) {
            String namaDaftar = riwayat ? "Riwayat" : "Aktif";
            lblInformasi.setText(namaDaftar + "  ·  " + daftarPesanan.size() + " pesanan");
        } else if (riwayat) {
            lblInformasi.setText("Belum ada riwayat pesanan.");
        } else if (pengguna.getPeran() == Pengguna.Peran.PENGANTAR) {
            lblInformasi.setText("Belum ada tugas pengantaran.");
        } else {
            lblInformasi.setText("Belum ada pesanan aktif.");
        }
    }

    private void isiPengantar() throws SQLException {
        Pengguna terpilih = (Pengguna) cmbPengantar.getSelectedItem();
        List<Pengguna> daftar = penggunaDAO.daftarPengantar();
        cmbPengantar.removeAllItems();
        for (Pengguna pengantar : daftar) {
            cmbPengantar.addItem(pengantar);
            if (terpilih != null && terpilih.getId() == pengantar.getId()) {
                cmbPengantar.setSelectedItem(pengantar);
            }
        }
    }

    private void isiTabel(Pesanan terpilih) {
        DateTimeFormatter formatWaktu = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
        modelTabel.setRowCount(0);
        for (int baris = 0; baris < daftarPesanan.size(); baris++) {
            Pesanan pesanan = daftarPesanan.get(baris);
            String pengantar = pesanan.getNamaPengantar() == null ? "—" : pesanan.getNamaPengantar();
            modelTabel.addRow(new Object[]{
                    pesanan.getStatus(), pesanan.getPenerima(), pesanan.getJumlah(), pesanan.getAlamat(),
                    Pesanan.formatRupiah(pesanan.getTotalHarga()), pengantar,
                    pesanan.getNomorHp(), formatWaktu.format(pesanan.getWaktuPemesanan()), pesanan.getId(),
                    pesanan.getAlasanPembatalan() == null ? "—" : pesanan.getAlasanPembatalan()
            });
            if (terpilih != null && terpilih.getId() == pesanan.getId()) {
                tabelPesanan.setRowSelectionInterval(baris, baris);
            }
        }
    }

    private Pesanan pesananTerpilih() {
        int baris = tabelPesanan.getSelectedRow();
        return baris >= 0 && baris < daftarPesanan.size() ? daftarPesanan.get(baris) : null;
    }

    private void aturTombolPesanan() {
        Pesanan terpilih = pesananTerpilih();
        Pesanan.Status status = null;
        if (!btnRiwayat.isSelected() && terpilih != null) {
            status = terpilih.getStatus();
        }
        btnBatalkan.setEnabled(status != null && status.bisaDibatalkanOleh(pengguna.getPeran()));
        btnTerimaTugaskan.setEnabled(status == Pesanan.Status.MENUNGGU_KONFIRMASI
                && cmbPengantar.getSelectedItem() != null);
        btnMulaiAntar.setEnabled(status == Pesanan.Status.DIPROSES);
        btnSelesaikan.setEnabled(status == Pesanan.Status.SEDANG_DIANTAR);
    }

    private void buatPesanan() {
        FormPesanan form = new FormPesanan(this, pengguna);
        form.setVisible(true);
        if (form.isTersimpan()) {
            if (btnAktif.isSelected()) {
                muatTabTerpilih();
            } else {
                btnAktif.setSelected(true);
            }
        }
    }

    private void tambahPengantar() {
        if (pengguna.getPeran() != Pengguna.Peran.ADMIN) {
            return;
        }
        DaftarAkunForm form = new DaftarAkunForm(this, pengguna);
        form.setVisible(true);
        if (form.getEmailTerdaftar() != null) {
            try {
                isiPengantar();
            } catch (SQLException e) {
                Tampilan.galatDatabase(this, e);
            }
        }
    }

    private void terimaDanTugaskan() {
        Pesanan pesanan = pesananTerpilih();
        Pengguna pengantar = (Pengguna) cmbPengantar.getSelectedItem();
        if (pesanan == null || pengantar == null) {
            return;
        }
        try {
            boolean berhasil = pesananDAO.terimaDanTugaskan(pengguna, pesanan.getId(), pengantar.getId());
            tampilkanHasilAksi(berhasil, "Pesanan diterima dan ditugaskan kepada " + pengantar + ".");
        } catch (SQLException e) {
            Tampilan.galatDatabase(this, e);
        }
    }

    private void mulaiAntar() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            return;
        }
        try {
            boolean berhasil = pesananDAO.mulaiAntar(pengguna, pesanan.getId());
            tampilkanHasilAksi(berhasil, "Status pesanan berubah menjadi Sedang diantar.");
        } catch (SQLException e) {
            Tampilan.galatDatabase(this, e);
        }
    }

    private void selesaikanPesanan() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            return;
        }
        String[] pilihanKonfirmasi = {"Sudah diterima", "Kembali"};
        int pilihan = JOptionPane.showOptionDialog(this,
                "Apakah galon untuk " + pesanan.getPenerima() + " sudah diterima?",
                "Konfirmasi penerimaan", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, pilihanKonfirmasi, pilihanKonfirmasi[1]);
        if (pilihan != 0) {
            return;
        }
        try {
            boolean berhasil = pesananDAO.selesaikanPesanan(pengguna, pesanan.getId());
            tampilkanHasilAksi(berhasil, "Pesanan selesai dan tersedia di Riwayat.");
        } catch (SQLException e) {
            Tampilan.galatDatabase(this, e);
        }
    }

    private void tampilkanHasilAksi(boolean berhasil, String pesan) {
        muatUlang(true);
        if (berhasil) {
            JOptionPane.showMessageDialog(this, pesan, "Pesanan diperbarui", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Pesanan sudah berubah atau aksi tidak diizinkan.",
                    "Pesanan gagal diperbarui", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void batalkan() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null || !pesanan.getStatus().bisaDibatalkanOleh(pengguna.getPeran())) {
            return;
        }
        String judul = "Batalkan pesanan #" + pesanan.getId();
        String alasan = pengguna.getPeran() == Pengguna.Peran.PENGANTAR
                ? "Penerima tidak ada di lokasi." : "";
        String petunjuk = "Isi alasan pembatalan (maksimal 300 karakter).";
        do {
            alasan = (String) JOptionPane.showInputDialog(this, petunjuk,
                    judul, JOptionPane.QUESTION_MESSAGE, null, null, alasan);
            if (alasan == null) {
                return;
            }
            alasan = alasan.strip();
            petunjuk = "Alasan belum sesuai. Isi 1 sampai 300 karakter.";
        } while (alasan.isEmpty() || alasan.length() > 300);

        String pesan = "Batalkan pesanan #" + pesanan.getId() + " untuk " + pesanan.getPenerima() + "?";
        if (pengguna.getPeran() == Pengguna.Peran.ADMIN && pesanan.getStatus() == Pesanan.Status.SEDANG_DIANTAR) {
            pesan += "\nPesanan sedang diantar. Hubungi pengantar sebelum membatalkannya.";
        }
        if (!Tampilan.konfirmasi(this, pesan + "\nAlasan: " + alasan)) {
            return;
        }
        try {
            boolean berhasil = pesananDAO.batalkan(pengguna, pesanan, alasan);
            tampilkanHasilAksi(berhasil, "Pesanan dibatalkan. Alasannya bisa dilihat di Riwayat.");
        } catch (SQLException e) {
            Tampilan.galatDatabase(this, e);
        }
    }

    private void keluar() {
        if (!Tampilan.konfirmasi(this, "Keluar dari akun ini?")) {
            return;
        }
        new LoginForm().setVisible(true);
        dispose();
    }
}
