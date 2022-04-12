package org.jbei.ice.account;

import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.utils.Utils;

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

//    public static String encryptNewUserPassword(String password, String salt) {
//        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(salt))
//            throw new IllegalArgumentException("Password and salt cannot be empty");
//
//        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 20000, 160);
//
//        try {
//            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//            byte[] hash = f.generateSecret(spec).getEncoded();
//            return bytesToHex(hash);
//        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//            Logger.error(e);
//            return null;
//        }
//    }

//    protected static String bytesToHex(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        char[] hexArray = "0123456789ABCDEF".toCharArray();
//
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }

    public static AccountModel fromDTO(Account info) {
        AccountModel account = new AccountModel();
        account.setFirstName(info.getFirstName());
        account.setLastName(info.getLastName());
        account.setInitials(info.getInitials());
        account.setEmail(info.getEmail().trim().toLowerCase());
        account.setDescription(info.getDescription());
        account.setInstitution(info.getInstitution());
        account.setIp("");
        return account;
    }
}
