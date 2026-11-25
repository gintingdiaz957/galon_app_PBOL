package id.pbol.galon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class KoneksiDatabase {
    // Sesuaikan URL, nama pengguna, dan password dengan pengaturan MySQL kalian.
    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/galon_app_basic?connectTimeout=3000&socketTimeout=5000";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private KoneksiDatabase() {
    }

    public static Connection buka() throws SQLException {
        return DriverManager.getConnection(
                System.getenv().getOrDefault("GALON_DB_URL", URL),
                System.getenv().getOrDefault("GALON_DB_USER", USER),
                System.getenv().getOrDefault("GALON_DB_PASSWORD", PASSWORD)
        );
    }
}
