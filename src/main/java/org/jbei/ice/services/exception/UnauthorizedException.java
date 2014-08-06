package org.jbei.ice.services.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Creates a "401 - Unauthorized" exception
 *
 * @author Hector Plahar
 */
public class UnauthorizedException extends WebApplicationException {

    public UnauthorizedException() {
        super(Response.Status.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {  // TODO : use json object and add developer message
        super(Response.status(Response.Status.UNAUTHORIZED).entity(message).type(MediaType.APPLICATION_JSON_TYPE)
                      .build());
    }
}
