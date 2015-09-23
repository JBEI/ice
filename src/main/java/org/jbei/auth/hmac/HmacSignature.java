/**
 *
 */
package org.jbei.auth.hmac;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class to handle generating HMAC signatures in REST requests. A signature should be
 * created by concatenating information on a request, then signing with a shared SecretKey.
 *
 * @author wcmorrell
 * @version 1.0
 * @since 1.0
 */
public interface HmacSignature {

    /**
     * @param stream an InputStream which data should pass through signature calculation
     * @return a wrapped stream to be used instead of the original stream
     */
    public abstract InputStream filterInput(final InputStream stream);

    /**
     * @param stream an OutputStream which data should pass through signature calculation
     * @return a wrapped stream to be used instead of the original stream
     */
    public abstract OutputStream filterOutput(final OutputStream stream);

    /**
     * @return compute the signature string
     */
    public abstract String generateSignature();

    /**
     * @return the user ID attached to the signature
     */
    public abstract String getUserId();

}
