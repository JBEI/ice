package org.jbei.ice.lib.account;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * Utility class for account management
 *
 * @author Hector Plahar
 */
public class AccountUtils {

    public static String encryptNewUserPassword(String password, String salt) {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(salt))
            throw new IllegalArgumentException("Password and salt cannot be empty");

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 20000, 160);

        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = f.generateSecret(spec).getEncoded();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Logger.error(e);
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        char[] hexArray = "0123456789ABCDEF".toCharArray();

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
