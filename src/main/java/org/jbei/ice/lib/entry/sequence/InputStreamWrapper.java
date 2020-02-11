package org.jbei.ice.lib.entry.sequence;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class InputStreamWrapper {

    private final InputStream inputStream;
    private String name;

    public InputStreamWrapper(InputStream inputStream, String name) {
        this.inputStream = inputStream;
        this.name = name;
    }

    public InputStreamWrapper(byte[] bytes, String name) {
        inputStream = new ByteArrayInputStream(bytes);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
