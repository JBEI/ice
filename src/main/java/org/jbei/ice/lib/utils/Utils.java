package org.jbei.ice.lib.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.ConfigurationKey;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * General utility methods.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
public class Utils {

    /**
     * Concatenate a Collection of Strings using the given delimiter.
     * <p/>
     * Analogous to python's string.join method
     *
     * @param s         Collection to work on
     * @param delimiter Delimiter to use to join
     * @return Joined string.
     */
    public static String join(String delimiter, Collection<?> s) {
        StringBuilder buffer = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item != null) {
                buffer.append(item);
                if (iter.hasNext()) {
                    buffer.append(delimiter);
                }
            }

        }
        return buffer.toString();
    }

    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    /**
     * Convert byte array to Hex notation.
     * <p/>
     * From a forum answer.
     *
     * @param bytes bytes to convert.
     * @return String of Hex representation
     * @throws UnsupportedEncodingException
     */
    public static String getHexString(byte[] bytes) throws UnsupportedEncodingException {
        byte[] HEX_CHAR_TABLE = {(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
                (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
                (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
        };

        byte[] hex = new byte[2 * bytes.length];
        int index = 0;

        for (byte b : bytes) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex, "ASCII");
    }

    /**
     * Retrieve the stack trace from the given Throwable, and output the string.
     *
     * @param throwable Throwable to process.
     * @return String output of the thorwable's stack trace.
     */
    public static String stackTraceToString(Throwable throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        throwable.printStackTrace(printWriter);

        return result.toString();
    }

    /**
     * Calculate the SHA-1 hash of the given string.
     *
     * @param string Plain text to hash.
     * @return Hex digest of give string.
     */
    public static String encryptSHA(String string) {
        return encrypt(string, "SHA-1");
    }

    /**
     * Calculate the SHA-256 hash of the given string.
     *
     * @param string plain text to hash.
     * @return Hex digest of the given string.
     */
    public static String encryptSha256(String string) {
        return encrypt(string, "SHA-256");
    }

    /**
     * Calculate the message digest of the given message string using the given algorithm.
     *
     * @param string    Plain text message.
     * @param algorithm Algorithm to be used.
     * @return Hex digest of the given string.
     */
    private static String encrypt(String string, String algorithm) {
        String result = "";

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(string.getBytes("UTF-8"));
            byte[] hashed = digest.digest();
            result = Utils.getHexString(hashed);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            Logger.error(e);
        }

        return result;
    }

    /**
     * Generate a random UUID.
     *
     * @return Hex digest of a UUID.
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateSaltForUserAccount() {
        return RandomStringUtils.randomAscii(30);
    }

    public static String getConfigValue(ConfigurationKey key) {
        ConfigurationController controller = new ConfigurationController();
        String value = controller.getPropertyValue(key);
        if (value != null)
            return value;
        return key.getDefaultValue();
    }
}
