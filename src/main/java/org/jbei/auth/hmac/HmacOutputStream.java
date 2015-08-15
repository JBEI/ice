package org.jbei.auth.hmac;

import javax.crypto.Mac;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Decorates an {@link OutputStream} to pass all written bytes through a {@link Mac}.
 *
 * @author wcmorrell
 * @version 1.0
 * @since 1.0
 */
public final class HmacOutputStream extends FilterOutputStream {

    private final Mac mac;

    /**
     * @param stream the stream to wrap
     * @param mac    the digest to update
     */
    public HmacOutputStream(final OutputStream stream, final Mac mac) {
        super(stream);
        this.mac = mac;
    }

    @Override
    public void write(final int data) throws IOException {
        mac.update((byte) data);
        super.write(data);
    }

    @Override
    public void write(final byte[] data, final int offset, final int length) throws IOException {
        mac.update(data, offset, length);
        super.write(data, offset, length);
    }

}