package org.jbei.ice.utils;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;

public class PasswordUtils {
    private static final int HASH_BYTE_SIZE = 160;
    private static final int SALT_BYTE_SIZE = 32;
    private static final int PBKDF2_ITERATIONS = 20000;
    private static final int TOKEN_BYTE_SIZE = 128;

    private static class Holder {
        static final SecureRandom random = new SecureRandom();
    }

    public static String encryptPassword(String password, String salt) throws UtilityException {
        if (password == null || password.trim().isEmpty() || salt == null || salt.trim().isEmpty())
            throw new NullPointerException("Password and/or salt cannot be empty");

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(Charset.defaultCharset()),
                PBKDF2_ITERATIONS, HASH_BYTE_SIZE);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            return String.valueOf(Hex.encodeHex(hash));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new UtilityException(e);
        }
    }

    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTE_SIZE];
        Holder.random.nextBytes(salt);
        return String.valueOf(Hex.encodeHex(salt));
    }

    public static String generateTemporaryPassword() {
        char[] arr = UUID.randomUUID().toString().substring(24).toCharArray();
        boolean converted = false;
        for (int i = 0; i < arr.length; i += 1) {
            if (arr[i] >= 'a' && arr[i] <= 'z') {
                arr[i] = (char) (arr[i] - 32);
                if (converted)
                    break;
                converted = true;
            }
        }
        return String.copyValueOf(arr);
    }

    public static String generateRandomToken(int byteSize) {
        byte[] token = new byte[byteSize];
        Holder.random.nextBytes(token);
        return DatatypeConverter.printBase64Binary(token);
    }
}
