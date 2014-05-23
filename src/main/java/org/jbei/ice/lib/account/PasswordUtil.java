package org.jbei.ice.lib.account;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.jbei.ice.lib.utils.UtilityException;

import org.apache.commons.codec.binary.Hex;

/**
 * Utility class for handling account passwords
 *
 * @author Hector Plahar
 */
public class PasswordUtil {

    private static final int HASH_BYTE_SIZE = 160;
    private static final int SALT_BYTE_SIZE = 32;
    private static final int PBKDF2_ITERATIONS = 20000;

    public static String encryptPassword(String password, String salt) throws UtilityException {
        if (password == null || password.trim().isEmpty() || salt == null || salt.trim().isEmpty())
            throw new NullPointerException("Password and/or salt cannot be empty");

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), PBKDF2_ITERATIONS, HASH_BYTE_SIZE);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new UtilityException(e);
        }
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);
        return Hex.encodeHexString(salt);
    }

    public static String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(24);
    }
}
