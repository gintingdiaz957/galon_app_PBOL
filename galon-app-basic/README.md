# Sistem Pemesanan dan Pengantaran Galon Air

Proyek PBOL desktop dengan Java Swing, Swing UI Designer, JDBC, dan MySQL. Target proyek Java 17. JDK 21 dapat dipakai untuk build dan menjalankannya.

## Menyiapkan proyek

1. Di IntelliJ IDEA, pilih **File → Open** dan buka folder `galon-app-basic`. Di **File → Project Structure → Project**, pilih JDK 21 yang terpasang dan pastikan **Language level** adalah **17**.
2. Gunakan MySQL 8.0.16 atau lebih baru. Di phpMyAdmin, pilih **Impor**. Untuk pemasangan baru, jalankan [galon_app_basic.sql](database/galon_app_basic.sql). Untuk database proyek versi lama, jalankan [migrasi_pembatalan.sql](database/migrasi_pembatalan.sql) satu kali. Migrasi mempertahankan pesanan dan akun.
3. Modul sudah menunjuk ke `lib/mysql-connector-j-9.7.0.jar`. Jika perlu memasang ulang atau menggantinya, buka **File → Project Structure → Modules → galon-app-basic → Dependencies → + → JARs or Directories**, pilih file Connector/J, pastikan cakupannya **Compile**, lalu klik **Apply**. Hapus entri JAR lama jika kamu menggantinya.
4. Di [KoneksiDatabase.java](src/id/pbol/galon/KoneksiDatabase.java), sesuaikan `URL`, `USER`, dan `PASSWORD` dengan MySQL lokal. Contoh bawaan memakai `127.0.0.1:3306`, database `galon_app_basic`, dan pengguna `root` dengan password kosong. Opsional, isi `GALON_DB_URL`, `GALON_DB_USER`, atau `GALON_DB_PASSWORD` di **Run → Edit Configurations → Environment variables** untuk menyesuaikan koneksi tanpa mengedit Java.
5. Di **Settings → Plugins**, pasang atau aktifkan **Swing UI Designer**. Buka `LoginForm.form`, `DaftarAkunForm.form`, `DaftarPesananForm.form`, dan `FormPesanan.form` dari panel Project. Di **Settings → Editor → GUI Designer**, gunakan **Generate GUI into → Binary class files**. IntelliJ akan menginisialisasi komponen yang terikat sebelum kode konstruktor memasang listener. [Panduan Swing UI Designer](https://www.jetbrains.com/help/idea/design-gui-using-swing.html)
6. Pilih **Build → Build Project**. Jalankan konfigurasi **Galon App**, atau klik ikon Run di samping `Main.main()` pada [Main.java](src/id/pbol/galon/Main.java).

## Akun demo

| Peran | Email | Kata sandi |
| --- | --- | --- |
| Konsumen | `konsumen@galon.test` | `Konsumen123!` |
| Admin | `admin@galon.test` | `Admin123!` |
| Pengantar | `pengantar@galon.test` | `Pengantar123!` |

## Mencoba alur di satu laptop

1. Masuk sebagai konsumen dan klik **Buat pesanan**. Isi nomor HP, alamat, dan jumlah. Nama penerima diambil dari akun konsumen. Akun konsumen baru dapat dibuat lewat **Daftar akun** di halaman masuk. Untuk 2 galon, pastikan tertulis **Rp14.000** sebelum menekan **Simpan pesanan**. Lihat pesanan baru di **Aktif** dengan status **Menunggu konfirmasi**.
2. Klik **Keluar**, lalu masuk sebagai admin. Pilih pesanan tadi dan pilih **Pengantar Demo**. Klik **Terima & Tugaskan**. Status menjadi **Diproses** dan jumlah **Menunggu konfirmasi** berkurang. Untuk menguji pembaruan otomatis, jalankan dua jendela aplikasi, satu sebagai admin dan satu sebagai konsumen. Buat pesanan dari akun konsumen dan tunggu sekitar 5 detik hingga pesanan muncul di jendela admin. Tombol **Muat ulang** memperbarui data kapan saja.
3. Masuk sebagai pengantar. Pilih pesanan yang ditugaskan. Arahkan kursor ke sel **Alamat** untuk membaca alamat lengkap. Klik **Mulai antar**. Status berubah menjadi **Sedang diantar**. Klik **Selesaikan**, lalu pilih **Sudah diterima** pada konfirmasi. Buka **Riwayat** untuk melihat status **Selesai**.
4. Tutup dan jalankan kembali aplikasi. Buka **Riwayat** sebagai konsumen, admin, atau pengantar yang menangani pesanan. Pesanan tetap tampil.
   Jika penerima tidak ada di lokasi, pengantar dapat memilih pesanan berstatus **Sedang diantar** miliknya, menekan **Batalkan pesanan**, lalu mengonfirmasi alasan. Pesanan masuk ke **Riwayat** dengan status **Dibatalkan**.
5. Untuk mencoba pembatalan, pilih pesanan aktif sebagai admin. Klik **Batalkan pesanan**, isi alasan, lalu konfirmasi. Untuk pesanan yang sedang diantar, hubungi pengantar lebih dahulu. Buka **Riwayat** dan lihat kolom **Alasan pembatalan**.

Kelas form menangani tombol dan tampilan. `PenggunaDAO` membaca akun. `PesananDAO` menyimpan, menampilkan, dan memperbarui pesanan. Setiap pembaruan memeriksa peran, penugasan, dan status sebelumnya melalui SQL.
