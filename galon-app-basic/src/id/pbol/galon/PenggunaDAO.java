package id.pbol.galon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PenggunaDAO {
    public Pengguna login(String email, char[] password) throws SQLException {
        String sql = "SELECT id, nama, peran, password_hash FROM pengguna WHERE email = ?";

        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, email);
            try (ResultSet hasil = perintah.executeQuery()) {
                if (hasil.next() && PasswordHash.cocok(password, hasil.getString("password_hash"))) {
                    return bacaPengguna(hasil);
                }
                return null;
            }
        }
    }

    public boolean daftarKonsumen(String nama, String email, char[] password) throws SQLException {
        String sql = "INSERT INTO pengguna (nama, email, password_hash, peran) VALUES (?, ?, ?, 'KONSUMEN')";
        return simpanAkun(sql, nama, email, password, null);
    }

    public boolean tambahPengantar(Pengguna admin, String nama, String email, char[] password) throws SQLException {
        if (admin == null || admin.getPeran() != Pengguna.Peran.ADMIN) {
            throw new SecurityException("Hanya admin yang dapat menambah pengantar.");
        }
        String sql = """
                INSERT INTO pengguna (nama, email, password_hash, peran)
                SELECT ?, ?, ?, 'PENGANTAR' FROM pengguna
                WHERE id = ? AND peran = 'ADMIN'
                """;
        return simpanAkun(sql, nama, email, password, admin);
    }

    private boolean simpanAkun(String sql, String nama, String email, char[] password, Pengguna admin)
            throws SQLException {
        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql)) {
            perintah.setString(1, nama);
            perintah.setString(2, email);
            perintah.setString(3, PasswordHash.buat(password));
            if (admin != null) {
                perintah.setInt(4, admin.getId());
            }
            if (perintah.executeUpdate() == 0) {
                throw new SecurityException("Akun admin sudah tidak memiliki akses.");
            }
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                return false;
            }
            throw e;
        }
    }

    public List<Pengguna> daftarPengantar() throws SQLException {
        String sql = "SELECT id, nama, peran FROM pengguna WHERE peran = 'PENGANTAR' ORDER BY nama, id";
        List<Pengguna> pengantar = new ArrayList<>();

        try (Connection koneksi = KoneksiDatabase.buka();
             PreparedStatement perintah = koneksi.prepareStatement(sql);
             ResultSet hasil = perintah.executeQuery()) {
            while (hasil.next()) {
                pengantar.add(bacaPengguna(hasil));
            }
        }

        return pengantar;
    }

    private Pengguna bacaPengguna(ResultSet hasil) throws SQLException {
        return new Pengguna(hasil.getInt("id"), hasil.getString("nama"),
                Pengguna.Peran.valueOf(hasil.getString("peran")));
    }
}
