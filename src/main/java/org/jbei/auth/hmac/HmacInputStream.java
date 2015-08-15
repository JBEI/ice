package org.jbei.auth.hmac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Decorates an {@link InputStream} to pass all read bytes to a {@link Mac}.
 *
 * @author wcmorrell
 * @version 1.0
 * @since 1.0
 */
public final class HmacInputStream extends FilterInputStream {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Logger log = LoggerFactory.getLogger(HmacInputStream.class);

    private final Mac mac;

    /**
     * @param stream the wrapped stream
     * @param mac    the digest to update
     */
    public HmacInputStream(final InputStream stream, final Mac mac) {
        super(stream);
        this.mac = mac;
    }

    @Override
    public int read() throws IOException {
        final int data = super.read();
        if (data != -1) {
            mac.update((byte) data);
        }
        return data;
    }

    @Override
    public int read(final byte[] data, final int offset, final int length) throws IOException {
        final int read = super.read(data, offset, length);
        if (read != -1) {
            final String debug = new String(Arrays.copyOfRange(data, offset, offset + read), UTF8);
            mac.update(data, offset, read);
            log.debug("Stream data: " + debug);
        }
        return read;
    }

}