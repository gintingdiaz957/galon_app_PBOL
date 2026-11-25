-- Jalankan sekali pada database proyek versi lama (MySQL 8.0.16 ke atas).
USE galon_app_basic;

ALTER TABLE pesanan
    DROP CHECK cek_penugasan,
    MODIFY COLUMN status ENUM('MENUNGGU_KONFIRMASI', 'DIPROSES', 'SEDANG_DIANTAR', 'SELESAI', 'DIBATALKAN')
        NOT NULL DEFAULT 'MENUNGGU_KONFIRMASI',
    ADD COLUMN alasan_pembatalan VARCHAR(300) NULL AFTER status,
    ADD CONSTRAINT cek_penugasan CHECK (
        (status = 'MENUNGGU_KONFIRMASI' AND id_pengantar IS NULL)
        OR (status IN ('DIPROSES', 'SEDANG_DIANTAR', 'SELESAI') AND id_pengantar IS NOT NULL)
        OR status = 'DIBATALKAN'
    );
