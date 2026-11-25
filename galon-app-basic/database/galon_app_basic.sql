-- Jalankan dengan MySQL 8.0.16 atau lebih baru.
CREATE DATABASE IF NOT EXISTS galon_app_basic
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE galon_app_basic;

CREATE TABLE IF NOT EXISTS pengguna (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    peran ENUM('KONSUMEN', 'ADMIN', 'PENGANTAR') NOT NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS pesanan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_konsumen INT NOT NULL,
    id_pengantar INT NULL,
    nama_penerima VARCHAR(100) NOT NULL,
    nomor_hp VARCHAR(30) NOT NULL,
    alamat VARCHAR(300) NOT NULL,
    jumlah INT NOT NULL,
    harga_satuan INT NOT NULL,
    total_harga BIGINT NOT NULL,
    status ENUM('MENUNGGU_KONFIRMASI', 'DIPROSES', 'SEDANG_DIANTAR', 'SELESAI', 'DIBATALKAN')
        NOT NULL DEFAULT 'MENUNGGU_KONFIRMASI',
    alasan_pembatalan VARCHAR(300) NULL,
    waktu_pemesanan DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_konsumen) REFERENCES pengguna (id),
    FOREIGN KEY (id_pengantar) REFERENCES pengguna (id),
    INDEX idx_status (status),
    CONSTRAINT cek_harga CHECK (
        jumlah > 0 AND harga_satuan > 0 AND total_harga = CAST(jumlah AS SIGNED) * harga_satuan
    ),
    CONSTRAINT cek_penugasan CHECK (
        (status = 'MENUNGGU_KONFIRMASI' AND id_pengantar IS NULL)
        OR (status IN ('DIPROSES', 'SEDANG_DIANTAR', 'SELESAI') AND id_pengantar IS NOT NULL)
        OR status = 'DIBATALKAN'
    )
) ENGINE = InnoDB;

-- Akun yang sudah ada dipertahankan saat skrip dijalankan kembali.
-- Format hash: salt Base64 $ PBKDF2-HMAC-SHA256 Base64 (600.000 iterasi).
INSERT INTO pengguna (nama, email, password_hash, peran) VALUES
    ('Konsumen Demo', 'konsumen@galon.test', 'PCEpntLk5lWWVNI+15eLvw==$8RaggSj3ytIwERYqIHt/cMcuyQZ5Q+ny3XofA2802bM=', 'KONSUMEN'),
    ('Admin Demo', 'admin@galon.test', 'mwco0trDUIcU9Zua+NQmVg==$lKIRsh8stsOi0hy8X7eRz8j1Jyw00EvXujK7oM71EY8=', 'ADMIN'),
    ('Pengantar Demo', 'pengantar@galon.test', 'xPRFay55nq56lQNZ0pk3jA==$Tvfpl/RBhaHxcrdwnrbU44sv9uUbVNndjLdd41QGI+w=', 'PENGANTAR')
ON DUPLICATE KEY UPDATE email = pengguna.email;
