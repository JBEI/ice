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

    protected void log(String userId, String message) {
        Logger.info(userId + ": " + message);
    }

//    protected Response created() {
////        String href = (String)resource.get("href");
//        URI uri = URI.create(href);
//        return Response.created(uri).entity(resource).build();
//    }

}
