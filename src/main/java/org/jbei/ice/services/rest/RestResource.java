package org.jbei.ice.services.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.security.Key;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jbei.auth.Authorization;
import org.jbei.auth.KeyTable;
import org.jbei.auth.hmac.HmacAuthorizor;
import org.jbei.auth.hmac.HmacSignatureFactory;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;

/**
 * Parent class for all rest resource objects
 *
 * @author Hector Plahar
 */
public class RestResource {

    // do lookup by using existing configuration DATA_DIRECTORY to find key names => key data
    private static final KeyTable TABLE = new KeyTable() {

        // keys stored in /var/lib/tomcat6/data/rest-auth by default
        private final File directory = Paths.get(
                Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), "rest-auth").toFile();

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

    @HeaderParam(value = "X-ICE-Authentication-SessionId")
    private String sessionId;

    @HeaderParam(value = "Authorization")
    private String hmacHeader;

    @Context
    private HttpServletRequest request;

    private String userId = null;

    /**
     * Extract the User ID from header values in the resource request.
     *
     * @return a string User ID
     * @throws WebApplicationException
     *             for unauthorized access
     */
    protected String getUserId() {
        final Authorization auth;
        if (userId != null) {
            // we've already looked up the userId
            return userId;
        } else if ((auth = AUTHORIZOR.validate(request)).isValid()) {
            userId = auth.getUserId();
            // TODO validation of meaningful userId
            // e.g. "admin" account on EDD won't mean anything to ICE
        } else if (sessionId != null) {
            userId = SessionHandler.getUserIdBySession(sessionId);
        }
        if (userId == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * @param sessionHeader
     *            not used
     * @return the UserId of the user matching the session value
     * @throws WebApplicationException
     *             if there is no valid UserId
     * @deprecated use {@link #getUserId()} instead
     */
    @Deprecated
    protected String getUserIdFromSessionHeader(final String sessionHeader) {
        final String userId = SessionHandler.getUserIdBySession(sessionId);
        if (userId == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return userId;
    }

    protected Response respond(final Response.Status status, final Object obj) {
        if (obj == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(status).entity(obj).build();
    }

    protected Response respond(final Object object) {
        if (object == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(object).build();
    }

    protected Response respond(final Response.Status status) {
        return Response.status(status).build();
    }

    protected Response respond(final boolean success) {
        if (success) {
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Used to log user actions
     *
     * @param userId
     *            unique user identifier
     * @param message
     *            log message
     */
    protected void log(String userId, final String message) {
        if (userId == null) {
            userId = "Unknown";
        }
        Logger.info(userId + ": " + message);
    }
}
