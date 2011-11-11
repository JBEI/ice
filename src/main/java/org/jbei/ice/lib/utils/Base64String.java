package org.jbei.ice.lib.utils;

import java.io.Serializable;

import org.postgresql.util.Base64;

/**
 * Class to hold a base64 string.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class Base64String implements Serializable {
    private static final long serialVersionUID = 1L;

    private String data = "";

    /**
     * Convert the given bytes to Base64 encoded string and store into the object.
     * 
     * @param bytes
     */
    public void putBytes(byte[] bytes) {
        this.data = Base64.encodeBytes(bytes);
    }

    /**
     * Output the stored base64 encoded string as bytes.
     * 
     * @return bytes.
     */
    public byte[] getBytes() {
        byte[] bytes = Base64.decode(this.data);

        return bytes;
    }

    /**
     * Retrieve the base64 encoded data.
     * 
     * @return base64 encoded string.
     */
    public String getData() {
        return this.data;
    }

    /**
     * Output the base64 encoded data.
     */
    @Override
    public String toString() {
        return data;
    }
}
