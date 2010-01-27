package org.jbei.ice.lib.utils;

import java.io.Serializable;

import org.postgresql.util.Base64;

/* Class to hold Base64 encoded bytes
 * 
 */
public class Base64String implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String data = "";

    public void putBytes(byte[] bytes) {
        this.data = Base64.encodeBytes(bytes);
    }

    public byte[] getBytes() {
        byte[] bytes = Base64.decode(this.data);
        return bytes;
    }

    public String toString() {
        return data;
    }
}
