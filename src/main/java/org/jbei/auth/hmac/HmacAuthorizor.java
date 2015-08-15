/**
 *
 */
package org.jbei.auth.hmac;

import org.apache.commons.lang3.StringUtils;
import org.jbei.auth.Authorization;
import org.jbei.auth.KeyTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.security.SignatureException;
import java.util.Map;

/**
 * Creates {@link Authorization} objects based on HMAC values in HTTP Authorization headers.
 *
 * @author wcmorrell
 * @version 1.0
 */
public class HmacAuthorizor {

    private static final Logger log = LoggerFactory.getLogger(HmacAuthorizor.class);

    private final HmacSignatureFactory factory;

    /**
     * @param table where to look up keyId => key
     */
    public HmacAuthorizor(final KeyTable table) {
        factory = new HmacSignatureFactory(table);
    }

    /**
     * Creates an initial signature object based on the request and Authorization header value.
     *
     * @param auth
     * @param method
     * @param host
     * @param path
     * @param params
     * @return an initial signature, without request body bytes appended; or {@code null}.
     */
    public HmacSignature initSignature(final String auth, final String method, final String host,
                                       final String path, final Map<String, ? extends Iterable<String>> params) {
        final String[] parts = StringUtils.split(auth, ':');
        if (parts == null || parts.length == 0) {
            log.debug("No Authorization header found on request");
        } else if ("1".equals(parts[0]) && parts.length == 4) {
            try {
                return factory.buildSignature(parts[1], parts[2], method, host, path, params);
            } catch (final SignatureException e) {
                log.error("Cannot initialize signature", e);
            }
        } else {
            log.warn("Unknown Authorization header format: " + auth);
        }
        return null;
    }

    /**
     * Validates a request based on HTTP Authorization header.
     *
     * @param request the incoming request
     * @return {@link Authorization} object
     */
    public Authorization validate(final HttpServletRequest request) {
        final String auth = request.getHeader("Authorization");
        final String[] parts = StringUtils.split(auth, ':');
        if (parts == null || parts.length == 0) {
            log.debug("No Authorization header found on request");
        } else if ("1".equals(parts[0]) && parts.length == 4) {
            return version1(request, parts[1], parts[2], parts[3]);
        } else {
            log.warn("Unknown Authorization header format: " + auth);
        }
        return Authorization.INVALID;
    }

    private Authorization version1(final HttpServletRequest request, final String keyId,
                                   final String userId, final String signature) {
        try {
            final HmacSignature hmac = factory.buildSignature(request, keyId, userId);
            if (hmac == null) {
                // no valid keyId
            } else if (signature.equals(hmac.generateSignature())) {
                return new Authorization() {
                    @Override
                    public boolean isValid() {
                        return true;
                    }

                    @Override
                    public String getUserId() {
                        return userId;
                    }
                };
            }
        } catch (final SignatureException e) {
            log.warn("Signature failed in HmacAuthorizor: " + e.getMessage());
        }
        return Authorization.INVALID;
    }
}
