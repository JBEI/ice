package org.jbei.ice.lib.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.SelectionMarker;

/**
 * General utility methods.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class Utils {
    /**
     * Parse comma separated data into a LinkedHashSet.
     * 
     * @param data
     *            - Data to parse.
     * @return LinkedHashSet of strings.
     * */
    public final static LinkedHashSet<String> toHashSetFromCommaSeparatedString(String data) {

        LinkedHashSet<String> result = new LinkedHashSet<String>();
        String[] temp = data.split(",");

        for (String i : temp) {
            i = i.trim();
            if (i.length() > 0) {
                result.add(i);
            }
        }

        return result;
    }

    /**
     * Convert a Collection of {@link SelectionMarker}s into a comma separated list.
     * 
     * @param selectionMarkers
     *            - Collection of SelectionMarkers.
     * @return Comma separated string.
     */
    public final static String toCommaSeparatedStringFromSelectionMarkers(
            Collection<SelectionMarker> selectionMarkers) {
        String result = null;

        ArrayList<String> temp = new ArrayList<String>();

        for (SelectionMarker selectionMarker : selectionMarkers) {
            temp.add(selectionMarker.getName());
        }

        result = join(", ", temp);

        return result;
    }

    /**
     * Convert a Collection of {@link Link}s into a comma separated list.
     * 
     * @param links
     *            - Collection of Links.
     * @return Comma separated string.
     */
    public final static String toCommaSeparatedStringFromLinks(Collection<Link> links) {
        String result = null;

        ArrayList<String> temp = new ArrayList<String>();
        for (Link link : links) {
            temp.add(link.getLink());
        }
        result = join(", ", temp);

        return result;
    }

    /**
     * Convert a Collection of {@link Name}s into a comma separated list.
     * 
     * @param names
     *            - Collection of Names
     * @return Comma separated string.
     */
    public final static String toCommaSeparatedStringFromNames(Collection<Name> names) {
        String result = null;

        ArrayList<String> temp = new ArrayList<String>();
        for (Name name : names) {
            temp.add(name.getName());
        }

        result = join(", ", temp);

        return result;
    }

    /**
     * Convert a Collection of {@link PartNumber}s into a comma separated list.
     * 
     * @param partNumbers
     *            - Collection of PartNumbers.
     * @return Comma separated string.
     */
    public final static String toCommaSeparatedStringFromPartNumbers(
            Collection<PartNumber> partNumbers) {
        String result = null;

        ArrayList<String> temp = new ArrayList<String>();

        for (PartNumber partNumber : partNumbers) {
            temp.add(partNumber.getPartNumber());
        }

        result = join(", ", temp);

        return result;
    }

    /**
     * Convert a Collection of {@link EntryFundingSource}s into a comma separated list.
     * 
     * @param entryFundingSources
     *            - Collection of EntryFundingSources
     * @return Comma separated string.
     */
    public final static String toCommaSeparatedStringFromEntryFundingSources(
            Collection<EntryFundingSource> entryFundingSources) {
        String result = null;

        ArrayList<String> temp = new ArrayList<String>();

        for (EntryFundingSource entryFundingSource : entryFundingSources) {
            FundingSource fundingSource = entryFundingSource.getFundingSource();
            temp.add(fundingSource.getFundingSource());
            temp.add(fundingSource.getPrincipalInvestigator());
        }

        result = join(", ", temp);

        return result;
    }

    /**
     * Concatenate a Collection of Strings using the given delimiter.
     * <p>
     * Analogous to python's string.join method
     * 
     * @param s
     *            - Collection to work on
     * @param delimiter
     *            - Delimiter to use to join
     * @return Joined string.
     */
    public static String join(String delimiter, Collection<?> s) {
        StringBuffer buffer = new StringBuffer();
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

    /**
     * Convert byte array to Hex notation.
     * <p>
     * From a forum answer.
     * 
     * @param bytes
     *            - bytes to convert.
     * @return String of Hex representation
     * @throws UnsupportedEncodingException
     */
    public static String getHexString(byte[] bytes) throws UnsupportedEncodingException {

        byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
                (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
                (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };
        {
            byte[] hex = new byte[2 * bytes.length];
            int index = 0;

            for (byte b : bytes) {
                int v = b & 0xFF;
                hex[index++] = HEX_CHAR_TABLE[v >>> 4];
                hex[index++] = HEX_CHAR_TABLE[v & 0xF];
            }
            return new String(hex, "ASCII");
        }
    }

    /**
     * Retrieve the stack trace from the given Throwable, and output the string.
     * 
     * @param throwable
     *            - Throwable to process.
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
     * @param string
     *            - Plain text to hash.
     * @return Hex digest of give string.
     */
    public static String encryptSHA(String string) {
        return encrypt(string, "SHA-1");
    }

    /**
     * Calculate the MD5 hash of the given string.
     * 
     * @param string
     *            - Plain text to hash.
     * @return Hex digest of the given string.
     */
    @Deprecated
    public static String encryptMD5(String string) {
        return encrypt(string, "MD5");
    }

    /**
     * Calculate the SHA-256 hash of the given string.
     * 
     * @param string
     *            - plain text to hash.
     * @return Hex digest of the given string.
     */
    public static String encryptSha256(String string) {
        return encrypt(string, "SHA-256");
    }

    /**
     * Calculate the message digest of the given message string using the given algorithm.
     * 
     * @param string
     *            - Plain text message.
     * @param algorithm
     *            - Algorithm to be used.
     * @return Hex digest of the given string.
     */
    private static String encrypt(String string, String algorithm) {
        String result = "";

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            digest.update(string.getBytes("UTF-8"));

            byte[] hashed = digest.digest();

            result = Utils.getHexString(hashed);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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

    /**
     * Escape special javascript characters.
     * 
     * @param stringValue
     *            - input.
     * @return Escaped input.
     */
    public static String escapeSpecialJavascriptCharacters(String stringValue) {
        String result = "";

        if (!StringUtils.containsNone(stringValue, new char[] { '\'' })) {
            result = StringUtils.replace(stringValue, "'", "\\'");
        } else {
            result = stringValue;
        }

        return result;
    }

    /**
     * Convert a long value into int, safely.
     * 
     * @param l
     *            - long value to convert.
     * @return int value.
     */
    public static int safeLongToInt(long l) {
        int result = 0;
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Cannot convert this long to Integer. Value too large: " + String.valueOf(l));
        } else {
            result = (int) l;
        }
        return result;
    }
}
