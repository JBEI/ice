package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.auth.hmac.HmacSignature;
import org.jbei.ice.lib.access.TokenVerification;
import org.jbei.ice.lib.account.UserSessions;
import org.jbei.ice.lib.common.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Parent class for all rest resource objects
 *
 * @author Hector Plahar
 */
public class RestResource {

    protected final String AUTHENTICATION_PARAM_NAME = "X-ICE-Authentication-SessionId";
    protected final String WOR_PARTNER_TOKEN = "X-ICE-WOR-Token";
    protected final String API_KEY_TOKEN = "X-ICE-API-Token";        // token for validation
    protected final String API_KEY_USER = "X-ICE-API-Token-User";    // optional user
    protected final String API_KEY_CLIENT_ID = "X-ICE-API-Token-Client"; // client id

    // do lookup by using existing configuration DATA_DIRECTORY to find key names => key data
//    private static final KeyTable TABLE = new KeyTable() {
//
//        // keys stored in /var/lib/tomcat6/data/rest-auth by default
//        private final File directory;
//
//        {
//            // need to force-create a transaction to get the DATA_DIRECTORY config value
//            HibernateUtil.beginTransaction();
//            directory = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
//                    "rest-auth").toFile();
//            HibernateUtil.commitTransaction();
//        }
//
//        @Override
//        public Key getKey(final String keyId) {
//            try {
//                // find file named by keyId in the directory
//                final File keyFile = new File(directory, keyId);
//                // collect all lines in the file to a buffer
//                final StringBuilder encoded = new StringBuilder();
//                try (final FileReader reader = new FileReader(keyFile);
//                     final BufferedReader buffered = new BufferedReader(reader);) {
//                    String line;
//                    while ((line = buffered.readLine()) != null) {
//                        encoded.append(line);
//                    }
//                    // after reading all lines, decode value into a Key object
//                    return HmacSignatureFactory.decodeKey(encoded.toString());
//                }
//            } catch (final Throwable t) {
//                Logger.error("Failed to load rest-auth key " + keyId);
//            }
//            return null;
//        }
//    };

//    private static final HmacAuthorizor AUTHORIZOR = new HmacAuthorizor(TABLE);

    @HeaderParam(value = WOR_PARTNER_TOKEN)
    private String worPartnerToken;

    @HeaderParam(value = API_KEY_CLIENT_ID)
    private String apiClientId;

    @HeaderParam(value = API_KEY_USER)
    private String apiUser;

    @HeaderParam(value = API_KEY_TOKEN)
    private String apiToken;

    @HeaderParam(value = AUTHENTICATION_PARAM_NAME)
    private String sessionId;

    @HeaderParam(value = "Authorization")
    private String hmacHeader;

    @Context
    protected HttpServletRequest request;

    /**
     * Extract the User ID from header values in the resource request.
     *
     * @return a string User ID
     * @throws WebApplicationException for unauthorized access
     * @see #getUserId(boolean)
     * @see #getUserId(String)
     * @see #getUserId(String, boolean)
     */
    protected String getUserId() {
        return getUserId(false); // false = throw exception for unauthenticated users
    }

    /**
     * Extract the User ID from header values in the resource request.
     *
     * @return a string User ID, or null if no user is authenticated and <code>suppressException == true</code>
     * @param suppressException true to return null instead of throwing WebApplicationException if no user
     *                          is authenticated.
     * @throws WebApplicationException for unauthorized access
     * @see #getUserId(String)
     * @see #getUserId(String, boolean)
     */
    protected String getUserId(boolean suppressException) {
        return getUserId(sessionId, suppressException);
    }

    /**
     * Extract the User ID from a query parameter value or header values in the resource request.
     *
     * @param sessionId a session ID sent via query parameters
     * @return a string User ID
     * @throws WebApplicationException for unauthorized access
     * @see #getUserId(boolean)
     * @see #getUserId(String, boolean)
     */
    protected String getUserId(final String sessionId) {
        return getUserId(sessionId, false); // false= throw exception for un-authenticated user
    }

    /**
     * Extract the User ID from a query parameter value or header values in the resource request.
     *
     * @param sessionId a session ID sent via query parameters
     * @param suppressException true to return null instead of throwing WebApplicationException if no user
     *                          is authenticated.
     * @return a string User ID, or null if no user is authenticated and <code>suppressException == true</code>
     * @throws WebApplicationException for unauthorized access
     * @see #getUserId(boolean)
     * @see #getUserId(String)
     */
    protected String getUserId(final String sessionId, boolean suppressException) {
        String userId = UserSessions.getUserIdBySession(sessionId);
        if (!StringUtils.isEmpty(userId))
            return userId;

        // check api key
        if (!StringUtils.isEmpty(apiToken)) {
            String clientId = !StringUtils.isEmpty(apiClientId) ? apiClientId : request.getRemoteHost();

            TokenVerification tokenVerification = new TokenVerification();
            userId = tokenVerification.verifyAPIKey(apiToken, clientId, apiUser);

            // being a bit generous in terms of allowing other auth methods to be attempted even though apiToken is set
            if (userId != null)
                return userId;
        }

        // check hmac signature
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

        if ((userId == null) && !suppressException)
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        return userId;
    }

    protected void verifyWebPartnerUrl() {
        String clientId = !StringUtils.isEmpty(apiClientId) ? apiClientId : request.getRemoteHost();
        TokenVerification tokenVerification = new TokenVerification();
        if (!tokenVerification.verifyPartnerToken(clientId, worPartnerToken))
            throw new WebApplicationException(Response.Status.FORBIDDEN);
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
