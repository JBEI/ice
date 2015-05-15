package org.jbei.ice.lib.account;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;

/**
 *
 *
 * @author Hector Plahar
 */
public class TokenHash {

    private static final int HASH_BYTE_SIZE = 160;
    private static final int SALT_BYTE_SIZE = 32;
    private static final int PBKDF2_ITERATIONS = 20000;

    public TokenHash() {
    }

    public String encryptPassword(String password, String salt) {
        if (password == null || password.trim().isEmpty() || salt == null || salt.trim().isEmpty())
            throw new NullPointerException("Password and/or salt cannot be empty");

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), PBKDF2_ITERATIONS, HASH_BYTE_SIZE);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            return DatatypeConverter.printBase64Binary(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);
        return DatatypeConverter.printBase64Binary(salt);
    }

    public String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(24);
    }

    public String generateRandomToken() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[256];
        random.nextBytes(salt);
        return DatatypeConverter.printBase64Binary(salt);
    }
}
