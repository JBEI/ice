package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.auth.KeyTable;
import org.jbei.auth.hmac.HmacAuthorizor;
import org.jbei.auth.hmac.HmacSignature;
import org.jbei.auth.hmac.HmacSignatureFactory;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.security.Key;

/**
 * Parent class for all rest resource objects
 *
 * @author Hector Plahar
 */
public class RestResource {

    protected final String AUTHENTICATION_PARAM_NAME = "X-ICE-Authentication-SessionId";
    protected final String WOR_PARTNER_TOKEN = "X-ICE-WOR-Token";

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

    @HeaderParam(value = AUTHENTICATION_PARAM_NAME)
    private String sessionId;

    @HeaderParam(value = "Authorization")
    private String hmacHeader;

    @Context
    private HttpServletRequest request;

    /**
     * Extract the User ID from header values in the resource request.
     *
     * @return a string User ID
     * @throws WebApplicationException for unauthorized access
     */
    protected String getUserId() {
        return getUserId(sessionId);
    }

    /**
     * Extract the User ID from a query parameter value or header values in the resource request.
     *
     * @param sessionId a session ID sent via query parameters
     * @return a string User ID
     * @throws WebApplicationException for unauthorized access
     */
    protected String getUserId(final String sessionId) {
        String userId = SessionHandler.getUserIdBySession(sessionId);
        if (!StringUtils.isEmpty(userId))
            return userId;

        final Object hmac = request.getAttribute(AuthenticationInterceptor.HMAC_SIGNATURE);
        final Object valid = request.getAttribute(AuthenticationInterceptor.EXPECTED_SIGNATURE);
        if (hmac != null && hmac instanceof HmacSignature) {
            final HmacSignature generated = (HmacSignature) hmac;
            if (generated.generateSignature().equals(valid)) {
                // TODO validation of meaningful userId
                // e.g. "admin" account on EDD won't mean anything to ICE
                userId = generated.getUserId();
            }
        }

        if (userId == null)
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        return userId;
    }

    /**
     * Create a {@link Response} object from an entity object.
     *
     * @param status HTTP status code
     * @param obj    entity in response
     * @return a Response object for the resource request, uses 500 error response if entity is
     * {@code null}
     */
    protected Response respond(final Response.Status status, final Object obj) {
        if (obj == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(status).entity(obj).build();
    }

    /**
     * Create a {@link Response} object from an entity object.
     *
     * @param object entity in response
     * @return a 404 NOT FOUND if object is {@code null}, else a 200 OK response with the entity
     */
    protected Response respond(final Object object) {
        if (object == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(object).build();
    }

    /**
     * Create an empty {@link Response} object.
     *
     * @param status HTTP status code to use on the Response
     * @return a Response object for the resource request
     */
    protected Response respond(final Response.Status status) {
        return Response.status(status).build();
    }

    /**
     * Create an empty {@link Response} object.
     *
     * @param success success/failure flag
     * @return a 200 OK response if success is {@code true}, otherwise a 500 error response
     */
    protected Response respond(final boolean success) {
        if (success) {
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Used to log user actions
     *
     * @param userId  unique user identifier
     * @param message log message
     */
    protected void log(final String userId, final String message) {
        final String who = (userId == null) ? "Unknown" : userId;
        Logger.info(who + ": " + message);
    }
}
