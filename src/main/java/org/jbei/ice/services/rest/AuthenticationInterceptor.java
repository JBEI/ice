/**
 *
 */
package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.auth.KeyTable;
import org.jbei.auth.hmac.HmacAuthorizor;
import org.jbei.auth.hmac.HmacSignature;
import org.jbei.auth.hmac.HmacSignatureFactory;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.hibernate.HibernateUtil;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.Key;

/**
 * Filter watches for Authorization headers on incoming requests, and passes along data to build an
 * {@link HmacSignature} to validate the request.
 *
 * @author wcmorrell
 * @version 4.2
 * @since 4.2
 */
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationInterceptor implements ContainerRequestFilter, ReaderInterceptor {

    /**
     * Property/Attribute name for mapping the expected value of the generated signature.
     */
    public static final String EXPECTED_SIGNATURE = "org.jbei.auth.signature";

    /**
     * Property/Attribute name for mapping the {@link HmacSignature} object for the request.
     */
    public static final String HMAC_SIGNATURE = "org.jbei.auth.hmac";

    // do lookup by using existing configuration DATA_DIRECTORY to find key names => key data
    private static final KeyTable TABLE = new KeyTable() {

        // keys stored in /var/lib/tomcat6/data/rest-auth by default
        private final File directory;
        {
            // need to force-create a transaction to get the DATA_DIRECTORY config value
            HibernateUtil.beginTransaction();
            directory = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                    "rest-auth").toFile();
            HibernateUtil.commitTransaction();
        }

        @Override
        public Key getKey(final String keyId) {
            try {
                // find file named by keyId in the directory
                final File keyFile = new File(directory, keyId);
                // collect all lines in the file to a buffer
                final StringBuilder encoded = new StringBuilder();
                try (final FileReader reader = new FileReader(keyFile);
                        final BufferedReader buffered = new BufferedReader(reader);) {
                    String line;
                    while ((line = buffered.readLine()) != null) {
                        encoded.append(line);
                    }
                    // after reading all lines, decode value into a Key object
                    return HmacSignatureFactory.decodeKey(encoded.toString());
                }
            } catch (final Throwable t) {
                Logger.error("Failed to load rest-auth key " + keyId);
            }
            return null;
        }

    };

    private static final HmacAuthorizor AUTHORIZOR = new HmacAuthorizor(TABLE);

    @Override
    public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException,
            WebApplicationException {
        final Object hmac = context.getProperty(HMAC_SIGNATURE);
        if (hmac != null && hmac instanceof HmacSignature) {
            context.setInputStream(((HmacSignature) hmac).filterInput(context.getInputStream()));
        }
        return context.proceed();
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String hmac = requestContext.getHeaderString("Authorization");
        final String[] parts = StringUtils.split(hmac, ':');
        if (parts == null || parts.length == 0) {
            Logger.debug("No Authorization header found on request");
        } else if (!"1".equals(parts[0]) || parts.length != 4) {
            Logger.debug("Unknown Authorization header format");
        } else {
            final UriInfo uriInfo = requestContext.getUriInfo();
            final MultivaluedMap<String, String> params = uriInfo.getQueryParameters(false);
            final String method = requestContext.getMethod();
            final String host = requestContext.getHeaderString("Host");
            final String path = uriInfo.getAbsolutePath().getPath(); // uriInfo path is relative
            final HmacSignature sig = AUTHORIZOR.initSignature(hmac, method, host, path, params);
            requestContext.setProperty(HMAC_SIGNATURE, sig);
            requestContext.setProperty(EXPECTED_SIGNATURE, parts[3]);

        }
    }

}
