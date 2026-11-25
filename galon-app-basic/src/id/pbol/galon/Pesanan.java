package id.pbol.galon;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

public class Pesanan {
    public static final int HARGA_PER_GALON = 7_000;

    public enum Status {
        MENUNGGU_KONFIRMASI("Menunggu konfirmasi"),
        DIPROSES("Diproses"),
        SEDANG_DIANTAR("Sedang diantar"),
        SELESAI("Selesai"),
        DIBATALKAN("Dibatalkan");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public boolean aktif() {
            return this != SELESAI && this != DIBATALKAN;
        }

        public boolean bisaDibatalkanOleh(Pengguna.Peran peran) {
            return switch (peran) {
                case ADMIN -> aktif();
                case PENGANTAR -> this == SEDANG_DIANTAR;
                case KONSUMEN -> false;
            };
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final int id;
    private final String penerima;
    private final String nomorHp;
    private final String alamat;
    private final int jumlah;
    private final long totalHarga;
    private final Status status;
    private final String namaPengantar;
    private final String alasanPembatalan;
    private final LocalDateTime waktuPemesanan;

    public Pesanan(int id, String penerima, String nomorHp, String alamat, int jumlah,
                   long totalHarga, Status status, String namaPengantar, String alasanPembatalan,
                   LocalDateTime waktuPemesanan) {
        this.id = id;
        this.penerima = penerima;
        this.nomorHp = nomorHp;
        this.alamat = alamat;
        this.jumlah = jumlah;
        this.totalHarga = totalHarga;
        this.status = status;
        this.namaPengantar = namaPengantar;
        this.alasanPembatalan = alasanPembatalan;
        this.waktuPemesanan = waktuPemesanan;
    }

    public int getId() {
        return id;
    }

    public String getPenerima() {
        return penerima;
    }

    public String getNomorHp() {
        return nomorHp;
    }

    public String getAlamat() {
        return alamat;
    }

    public int getJumlah() {
        return jumlah;
    }

    public long getTotalHarga() {
        return totalHarga;
    }

    public Status getStatus() {
        return status;
    }

    public String getNamaPengantar() {
        return namaPengantar;
    }

    public String getAlasanPembatalan() {
        return alasanPembatalan;
    }

    public LocalDateTime getWaktuPemesanan() {
        return waktuPemesanan;
    }

    public static long hitungTotal(int jumlah) {
        return (long) jumlah * HARGA_PER_GALON;
    }

    public static String formatRupiah(long nilai) {
        return "Rp" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(nilai);
    }
}
