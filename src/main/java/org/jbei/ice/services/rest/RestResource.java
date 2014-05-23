package org.jbei.ice.services.rest;

import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.services.exception.UnauthorizedException;

/**
 * Parent class for all rest resource object
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

    protected Response respond(Response.Status status, IDataTransferModel obj) {
        return Response.status(status).entity(obj).build();
    }

    protected Response respond(Response.Status status) {
        return Response.status(status).build();
    }

//    protected Response created() {
////        String href = (String)resource.get("href");
//        URI uri = URI.create(href);
//        return Response.created(uri).entity(resource).build();
//    }

}
