package org.jbei.ice.lib.account;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.utils.Utils;

/**
 * Utility class for account management
 *
 * @author Hector Plahar
 */
public class AccountUtils {

    /**
     * Return the encrypted version of the given password, using the salt from the settings file.
     *
     * @param password non-empty string
     * @return 40 character encrypted string.
     */
    public static String encryptPassword(String password, String userSalt) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Cannot encrypt null or empty password");
        return Utils.encryptSHA(userSalt + password);
    }

    public static String encryptNewUserPassword(String password, String salt) {
        if (password == null || password.trim().isEmpty() || salt == null || salt.trim().isEmpty())
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

    protected static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        char[] hexArray = "0123456789ABCDEF".toCharArray();

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static Account fromDTO(AccountTransfer info) {
        Account account = new Account();
        account.setFirstName(info.getFirstName());
        account.setLastName(info.getLastName());
        account.setInitials(info.getInitials());
        account.setEmail(info.getEmail().trim());
        account.setDescription(info.getDescription());
        account.setInstitution(info.getInstitution());
        account.setIp("");
        return account;
    }
}
