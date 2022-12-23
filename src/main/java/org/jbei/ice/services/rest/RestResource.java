package org.jbei.ice.services.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.access.PermissionException;
import org.jbei.ice.access.TokenVerification;
import org.jbei.ice.account.UserSessions;
import org.jbei.ice.dto.web.RegistryPartner;
import org.jbei.ice.logging.Logger;

/**
 * Parent class for all rest resource objects.
 * Handles session validation as well as return responses based on specific values
 *
 * @author Hector Plahar
 */
public class RestResource {

    private final String AUTHENTICATION_PARAM_NAME = org.jbei.ice.services.rest.Headers.AUTHENTICATION_PARAM_NAME;
    final String WOR_PARTNER_TOKEN = org.jbei.ice.services.rest.Headers.WOR_PARTNER_TOKEN;
    private final String API_KEY_TOKEN = org.jbei.ice.services.rest.Headers.API_KEY_TOKEN;               // token for validation
    private final String API_KEY_USER = org.jbei.ice.services.rest.Headers.API_KEY_USER;           // optional user. system checks and uses assigned token user if not specified
    private final String API_KEY_CLIENT_ID = org.jbei.ice.services.rest.Headers.API_KEY_CLIENT_ID;    // client id
    private final String REMOTE_USER_TOKEN = org.jbei.ice.services.rest.Headers.REMOTE_USER_TOKEN;   // token for remote user
    private final String REMOTE_USER_ID = Headers.REMOTE_USER_ID;         // id for remote user

    @HeaderParam(value = WOR_PARTNER_TOKEN)
    protected String worPartnerToken;

    @HeaderParam(value = API_KEY_CLIENT_ID)
    protected String apiClientId;

    @HeaderParam(value = API_KEY_USER)
    protected String apiUser;

    @HeaderParam(value = API_KEY_TOKEN)
    protected String apiToken;

    @HeaderParam(value = AUTHENTICATION_PARAM_NAME)
    protected String sessionId;

    @HeaderParam(value = REMOTE_USER_TOKEN)
    protected String remoteUserToken;

    @HeaderParam(value = REMOTE_USER_ID)
    protected String remoteUserId;

    @QueryParam(value = "sid")
    protected String querySessionId;

    @Context
    protected HttpServletRequest request;

    /**
     * Extract the User ID from header values in the resource request. This can either
     * be the session id or API keys
     *
     * @return a string User ID
     * @throws WebApplicationException for unauthorized access
     */
    protected String getUserId() {
        return getUserId(sessionId);
    }

    /**
     * Attempts to retrieve the identifier for the specific user
     *
     * @return valid user identifier
     * @throws WebApplicationException with status 401 if the user id cannot be retrieved
     */
    protected String requireUserId() {
        String userId = getUserId();
        if (userId == null)
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        return userId;
    }

    /**
     * Requires either a valid user request or request from a web partner
     *
     * @param logMessage log message for request
     */
    void requireUserIdOrWebPartner(String logMessage) {
        String userId = getUserId();
        if (StringUtils.isNotEmpty(userId)) {
            log(userId, logMessage);
            return;
        }

        // try web partner
        RegistryPartner partner = getWebPartner();
        if (partner == null)
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);

        log(partner.getUrl(), logMessage);
    }

    // returns the  name and port for this server
    String getThisServer(boolean includeContext) {
        String url = request.getServerName();
        int port = request.getServerPort();
        // exclude invalid and default http(s) ports
        if (port > 0 && port != 443 && port != 80) {
            url += (":" + port);
        }

        if (includeContext) {
            String context = request.getContextPath();
            if (!StringUtils.isEmpty(context))
                url += "/" + context;
        }
        return url;
    }

    /**
     * Extract the User ID from a query parameter value or header values in the resource request.
     */
    protected String getUserId(String sessionId) {
        if (StringUtils.isEmpty(sessionId) && !StringUtils.isEmpty(querySessionId))
            sessionId = querySessionId;

        String userId = UserSessions.getUserIdBySession(sessionId);
        if (!StringUtils.isEmpty(userId))
            return userId;

        // check api key
        if (!StringUtils.isEmpty(apiToken)) {
            String clientId = !StringUtils.isEmpty(apiClientId) ? apiClientId : request.getRemoteHost();

            try {
                TokenVerification tokenVerification = new TokenVerification();
                userId = tokenVerification.verifyAPIKey(apiToken, clientId, apiUser);
            } catch (PermissionException pe) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            // being a bit generous in terms of allowing other auth methods to be attempted even though apiToken is set
            if (userId != null)
                return userId;
        }

        return userId;
    }

    RegistryPartner requireWebPartner() {
        RegistryPartner partner = getWebPartner();
        if (partner == null)
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        return partner;
    }

    private RegistryPartner getWebPartner() {
        String clientId = !StringUtils.isEmpty(apiClientId) ? apiClientId : request.getRemoteHost();
        TokenVerification tokenVerification = new TokenVerification();
        return tokenVerification.verifyPartnerToken(clientId, worPartnerToken);
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
        if (StringUtils.isEmpty(message))
            return;

        final String who = (userId == null) ? "Unknown" : userId;
        Logger.info(who + ": " + message);
    }

    Response addHeaders(Response.ResponseBuilder response, String fileName) {
        response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        if (StringUtils.isEmpty(fileName))
            return response.build();

        int dotIndex = fileName.lastIndexOf('.') + 1;
        if (dotIndex == 0)
            return response.build();

        String mimeType = ExtensionToMimeType.getMimeType(fileName.substring(dotIndex));
        response.header("Content-Type", mimeType + "; name=\"" + fileName + "\"");
        return response.build();
    }
}
