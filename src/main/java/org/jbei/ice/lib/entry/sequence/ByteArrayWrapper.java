package org.jbei.ice.lib.entry.sequence;

/**
 * Wrapper around an array of bytes representing file contents,
 * and the name of the file.
 *
 * @author Hector Plahar
 */
public class ByteArrayWrapper {

    private final byte[] bytes;
    private final String name;

    public ByteArrayWrapper(byte[] bytes, String name) {
        this.bytes = bytes;
        this.name = name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getName() {
        return name;
    }
}
