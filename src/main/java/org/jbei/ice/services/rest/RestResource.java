package org.jbei.ice.services.rest;

import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Parent class for all rest resource objects
 *
 * @author Hector Plahar
 */
public class RestResource {

    protected String getUserIdFromSessionHeader(String sessionHeader) {
        String userId = SessionHandler.getUserIdBySession(sessionHeader);
        if (userId == null)
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        return userId;
    }

    protected Response respond(Response.Status status, Object obj) {
        if (obj == null)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        return Response.status(status).entity(obj).build();
    }

    protected Response respond(Object object) {
        if (object == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.status(Response.Status.OK).entity(object).build();
    }

    protected Response respond(Response.Status status) {
        return Response.status(status).build();
    }

    protected Response respond(boolean success) {
        if (success)
            return Response.status(Response.Status.OK).build();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Used to log user actions
     *
     * @param userId  unique user identifier
     * @param message log message
     */
    protected void log(String userId, String message) {
        if (userId == null)
            userId = "Unknown";
        Logger.info(userId + ": " + message);
    }
}
