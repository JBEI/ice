package org.jbei.auth.hmac;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @author wcmorrell
 * @version 1.0
 */
public final class DefaultHmacSignature implements HmacSignature {

    private final Mac mac;
    private final String userId;

    private String signature = null;

    /**
     * @param mac
     * @param userId
     */
    public DefaultHmacSignature(final Mac mac, final String userId) {
        this.mac = mac;
        this.userId = userId;
    }

    @Override
    public InputStream filterInput(final InputStream stream) {
        return new HmacInputStream(stream, mac);
    }

    @Override
    public OutputStream filterOutput(final OutputStream stream) {
        return new HmacOutputStream(stream, mac);
    }

    @Override
    public String generateSignature() {
        if (signature == null) {
            final byte[] rawSignature = mac.doFinal();
            signature = Base64.encodeBase64String(rawSignature);
        }
        return signature;
    }

    @Override
    public String getUserId() {
        return userId;
    }

}