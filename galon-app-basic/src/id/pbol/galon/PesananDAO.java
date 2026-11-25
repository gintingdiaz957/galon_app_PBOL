package id.pbol.galon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PesananDAO {
    public boolean buatPesanan(Pengguna konsumen, String nomorHp,
                               String alamat, int jumlah) throws SQLException {
        if (konsumen.getPeran() != Pengguna.Peran.KONSUMEN) {
            return false;
        }
        if (nomorHp.isBlank() || alamat.isBlank() || jumlah <= 0) {
            throw new IllegalArgumentException("Data pesanan wajib diisi dengan benar.");
        }

        String sql = """
                INSERT INTO pesanan (id_konsumen, nama_penerima, nomor_hp, alamat,
                                     jumlah, harga_satuan, total_harga, status)
                SELECT id, nama, ?, ?, ?, ?, ?, 'MENUNGGU_KONFIRMASI'
                FROM pengguna WHERE id = ? AND peran = 'KONSUMEN'
                """;

        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, nomorHp);
            perintah.setString(2, alamat);
            perintah.setInt(3, jumlah);
            perintah.setInt(4, Pesanan.HARGA_PER_GALON);
            perintah.setLong(5, Pesanan.hitungTotal(jumlah));
            perintah.setInt(6, konsumen.getId());
            return perintah.executeUpdate() == 1;
        }
    }

    public List<Pesanan> daftarPesanan(Pengguna pengguna, boolean riwayat) throws SQLException {
        String filterStatus = riwayat ? "p.status IN ('SELESAI', 'DIBATALKAN')"
                : "p.status NOT IN ('SELESAI', 'DIBATALKAN')";
        String filterPeran = switch (pengguna.getPeran()) {
            case KONSUMEN -> " AND p.id_konsumen = ?";
            case PENGANTAR -> " AND p.id_pengantar = ?";
            case ADMIN -> "";
        };
        String sql = """
                SELECT p.id, p.nama_penerima, p.nomor_hp, p.alamat, p.jumlah, p.total_harga,
                       p.status, p.waktu_pemesanan, p.alasan_pembatalan, pengantar.nama AS nama_pengantar
                FROM pesanan p
                LEFT JOIN pengguna pengantar ON p.id_pengantar = pengantar.id
                WHERE
                """ + filterStatus + filterPeran + " ORDER BY p.waktu_pemesanan DESC, p.id DESC";
        List<Pesanan> daftar = new ArrayList<>();

        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            if (pengguna.getPeran() != Pengguna.Peran.ADMIN) {
                perintah.setInt(1, pengguna.getId());
            }
            try (ResultSet hasil = perintah.executeQuery()) {
                while (hasil.next()) {
                    daftar.add(new Pesanan(hasil.getInt("id"), hasil.getString("nama_penerima"),
                            hasil.getString("nomor_hp"), hasil.getString("alamat"), hasil.getInt("jumlah"),
                            hasil.getLong("total_harga"), Pesanan.Status.valueOf(hasil.getString("status")),
                            hasil.getString("nama_pengantar"), hasil.getString("alasan_pembatalan"),
                            hasil.getObject("waktu_pemesanan", LocalDateTime.class)));
                }
            }
        }
        return daftar;
    }

    public long jumlahMenunggu(Pengguna admin) throws SQLException {
        if (admin.getPeran() != Pengguna.Peran.ADMIN) {
            throw new IllegalArgumentException("Hanya admin yang bisa melihat jumlah pesanan masuk.");
        }

        String sql = "SELECT COUNT(*) FROM pesanan WHERE status = 'MENUNGGU_KONFIRMASI'";
        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql);
             ResultSet hasil = perintah.executeQuery()) {
            hasil.next();
            return hasil.getLong(1);
        }
    }

    public boolean terimaDanTugaskan(Pengguna admin, int idPesanan, int idPengantar) throws SQLException {
        if (admin.getPeran() != Pengguna.Peran.ADMIN) {
            return false;
        }

        // Periksa peran dan status di query yang sama dengan pembaruan.
        String sql = """
                UPDATE pesanan p
                JOIN pengguna admin ON admin.id = ? AND admin.peran = 'ADMIN'
                JOIN pengguna pengantar ON pengantar.id = ? AND pengantar.peran = 'PENGANTAR'
                SET p.id_pengantar = pengantar.id, p.status = 'DIPROSES'
                WHERE p.id = ? AND p.status = 'MENUNGGU_KONFIRMASI' AND p.id_pengantar IS NULL
                """;

        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setInt(1, admin.getId());
            perintah.setInt(2, idPengantar);
            perintah.setInt(3, idPesanan);
            return perintah.executeUpdate() == 1;
        }
    }

    public boolean batalkan(Pengguna pengguna, Pesanan pesanan, String alasan) throws SQLException {
        if (!pesanan.getStatus().bisaDibatalkanOleh(pengguna.getPeran())
                || alasan.isBlank() || alasan.length() > 300) {
            return false;
        }
        String sql = """
                UPDATE pesanan p
                JOIN pengguna u ON u.id = ? AND u.peran = ?
                SET p.status = 'DIBATALKAN', p.alasan_pembatalan = ?
                WHERE p.id = ? AND p.status = ?
                  AND (u.peran = 'ADMIN' OR (u.peran = 'PENGANTAR' AND p.id_pengantar = u.id))
                """;
        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setInt(1, pengguna.getId());
            perintah.setString(2, pengguna.getPeran().name());
            perintah.setString(3, alasan.strip());
            perintah.setInt(4, pesanan.getId());
            perintah.setString(5, pesanan.getStatus().name());
            return perintah.executeUpdate() == 1;
        }
    }

    public boolean mulaiAntar(Pengguna pengantar, int idPesanan) throws SQLException {
        return ubahStatusPengantaran(pengantar, idPesanan, Pesanan.Status.DIPROSES, Pesanan.Status.SEDANG_DIANTAR);
    }

    public boolean selesaikanPesanan(Pengguna pengantar, int idPesanan) throws SQLException {
        return ubahStatusPengantaran(pengantar, idPesanan, Pesanan.Status.SEDANG_DIANTAR, Pesanan.Status.SELESAI);
    }

    private boolean ubahStatusPengantaran(Pengguna pengantar, int idPesanan,
                                          Pesanan.Status statusAwal, Pesanan.Status statusBaru) throws SQLException {
        if (pengantar.getPeran() != Pengguna.Peran.PENGANTAR) {
            return false;
        }

        String sql = """
                UPDATE pesanan p
                JOIN pengguna pengantar ON p.id_pengantar = pengantar.id AND pengantar.peran = 'PENGANTAR'
                SET p.status = ?
                WHERE p.id = ? AND p.id_pengantar = ? AND p.status = ?
                """;

        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, statusBaru.name());
            perintah.setInt(2, idPesanan);
            perintah.setInt(3, pengantar.getId());
            perintah.setString(4, statusAwal.name());
            return perintah.executeUpdate() == 1;
        }
    }
}
