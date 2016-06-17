package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.auth.hmac.HmacSignature;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.TokenVerification;
import org.jbei.ice.lib.account.UserSessions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.web.RegistryPartner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Parent class for all rest resource objects.
 * Handles session validation as well as return responses based on specific values
 *
 * @author Hector Plahar
 */
public class RestResource {

    protected final String AUTHENTICATION_PARAM_NAME = Headers.AUTHENTICATION_PARAM_NAME;
    protected final String WOR_PARTNER_TOKEN = Headers.WOR_PARTNER_TOKEN;
    protected final String API_KEY_TOKEN = Headers.API_KEY_TOKEN;               // token for validation
    protected final String API_KEY_USER = Headers.API_KEY_USER;           // optional user. system checks and uses assigned token user if not specified
    protected final String API_KEY_CLIENT_ID = Headers.API_KEY_CLIENT_ID;    // client id
    protected final String REMOTE_USER_TOKEN = Headers.REMOTE_USER_TOKEN;   // token for remote user
    protected final String REMOTE_USER_ID = Headers.REMOTE_USER_ID;         // id for remote user

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

    @HeaderParam(value = "Authorization")
    protected String hmacHeader;

    @QueryParam(value = "sid")
    protected String querySessionId;

    @Context
    protected HttpServletRequest request;

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

    // returns the  name and port for this server
    protected String getThisServer(boolean includeContext) {
        String url = request.getServerName();
        int port = request.getServerPort();
        // exclude invalid and default http(s) ports
        if (port > 0 && port != 443 && port != 80) {
            url += (":" + Integer.toString(port));
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

        return userId;
    }

    protected RegistryPartner requireWebPartner() {
        RegistryPartner partner = getWebPartner();
        if (partner == null)
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        return partner;
    }

    protected RegistryPartner getWebPartner() {
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
        final String who = (userId == null) ? "Unknown" : userId;
        Logger.info(who + ": " + message);
    }

    protected Response addHeaders(Response.ResponseBuilder response, String fileName) {
        response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        int dotIndex = fileName.lastIndexOf('.') + 1;
        if (dotIndex == 0)
            return response.build();

        String mimeType = ExtensionToMimeType.getMimeType(fileName.substring(dotIndex));
        response.header("Content-Type", mimeType + "; name=\"" + fileName + "\"");
        return response.build();
    }
}
