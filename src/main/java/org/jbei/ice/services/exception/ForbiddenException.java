package org.jbei.ice.services.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Hector Plahar
 */
public class ForbiddenException extends WebApplicationException {
    public ForbiddenException() {
        super(Response.Status.FORBIDDEN);
    }

    public ForbiddenException(String message) {  // TODO : use json object and add developer message
        super(Response.status(Response.Status.FORBIDDEN).entity(message).type(MediaType.APPLICATION_JSON_TYPE)
                      .build());
    }
}
