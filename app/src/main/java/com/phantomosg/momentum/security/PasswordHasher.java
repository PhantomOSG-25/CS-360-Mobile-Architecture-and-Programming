package com.phantomosg.momentum.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private PasswordHasher() {
    }

    public static HashResult hash(String password) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] derived = derive(password.toCharArray(), salt);
        return new HashResult(
                Base64.getEncoder().encodeToString(derived),
                Base64.getEncoder().encodeToString(salt)
        );
    }

    public static boolean verify(String password, String expectedHash, String salt) {
        byte[] decodedSalt = Base64.getDecoder().decode(salt);
        byte[] actual = derive(password.toCharArray(), decodedSalt);
        byte[] expected = Base64.getDecoder().decode(expectedHash);
        if (actual.length != expected.length) return false;

        int difference = 0;
        for (int i = 0; i < actual.length; i++) {
            difference |= actual[i] ^ expected[i];
        }
        return difference == 0;
    }

    private static byte[] derive(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable", exception);
        } finally {
            spec.clearPassword();
        }
    }

    public static final class HashResult {
        private final String hash;
        private final String salt;

        public HashResult(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }

        public String getHash() {
            return hash;
        }

        public String getSalt() {
            return salt;
        }
    }
}

