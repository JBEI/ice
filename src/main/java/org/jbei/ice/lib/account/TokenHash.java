package org.jbei.ice.lib.account;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * @author Hector Plahar
 */
public class TokenHash {

    private static final int HASH_BYTE_SIZE = 160;
    private static final int SALT_BYTE_SIZE = 32;
    private static final int TOKEN_BYTE_SIZE = 128;
    private static final int PBKDF2_ITERATIONS = 20000;

    public String encrypt(String value, String salt) {
        if (value == null || value.trim().isEmpty() || salt == null || salt.trim().isEmpty())
            throw new NullPointerException("Cannot encrypt null value or salt");

        KeySpec spec = new PBEKeySpec(value.toCharArray(), salt.getBytes(), PBKDF2_ITERATIONS, HASH_BYTE_SIZE);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            return DatatypeConverter.printBase64Binary(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    /**
     * Uses {@link SecureRandom} to generate a random bytes of size 32
     * which is converted to a string containing a lexical base64 representation
     *
     * @return random bytes converted to a string
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);
        return DatatypeConverter.printBase64Binary(salt);
    }

    public String generateRandomToken() {
        SecureRandom random = new SecureRandom();
        byte[] token = new byte[TOKEN_BYTE_SIZE];
        random.nextBytes(token);
        return DatatypeConverter.printBase64Binary(token);
    }

    public String generateRandomToken(int byteSize) {
        SecureRandom random = new SecureRandom();
        byte[] token = new byte[byteSize];
        random.nextBytes(token);
        return DatatypeConverter.printBase64Binary(token);
    }
}
