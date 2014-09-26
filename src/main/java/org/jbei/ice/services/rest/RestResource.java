package org.jbei.ice.services.rest;

import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.services.exception.UnauthorizedException;

/**
 * Parent class for all rest resource objects
 *
 * @author Hector Plahar
 */
public class RestResource {

    protected String getUserIdFromSessionHeader(String sessionHeader) {
        String userId = SessionHandler.getUserIdBySession(sessionHeader);
        if (userId == null)
            throw new UnauthorizedException();
        return userId;
    }

    protected Response respond(Response.Status status, Object obj) {
        if (obj == null)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        return Response.status(status).entity(obj).build();
    }

    protected Response respond(Response.Status status) {
        return Response.status(status).build();
    }

    protected Response respond(boolean success) {
        if (success)
            return Response.status(Response.Status.OK).build();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    protected void log(String userId, String message) {
        Logger.info(userId + ": " + message);
    }
}
