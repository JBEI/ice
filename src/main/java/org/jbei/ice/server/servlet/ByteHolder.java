package org.jbei.ice.server.servlet;

import java.io.Serializable;

public class ByteHolder implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] bytes = null;
    private String name = null;

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
