package id.pbol.galon;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class PasswordHash {
    private static final int ITERASI = 600_000;

    private PasswordHash() {
    }

    private static byte[] hitung(char[] password, byte[] salt) {
        PBEKeySpec spesifikasi = new PBEKeySpec(password, salt, ITERASI, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spesifikasi).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 tidak tersedia pada JDK ini.", e);
        } finally {
            spesifikasi.clearPassword();
        }
    }

    static String buat(char[] password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = hitung(password, salt);
        try {
            return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    static boolean cocok(char[] password, String hashTersimpan) {
        // Format kolom password_hash: salt Base64, tanda $, hasil PBKDF2 Base64.
        String[] bagian = hashTersimpan.split("\\$", -1);
        if (bagian.length != 2) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(bagian[0]);
            byte[] hash = Base64.getDecoder().decode(bagian[1]);
            if (salt.length != 16 || hash.length != 32) {
                return false;
            }

            byte[] hasil = hitung(password, salt);
            try {
                return MessageDigest.isEqual(hash, hasil);
            } finally {
                Arrays.fill(hasil, (byte) 0);
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
