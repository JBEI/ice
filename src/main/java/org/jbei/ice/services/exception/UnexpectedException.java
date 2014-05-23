package org.jbei.ice.services.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Hector Plahar
 */
public class UnexpectedException extends WebApplicationException {

    public UnexpectedException() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }

    public UnexpectedException(String message) {  // TODO : use json object and add developer message
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).type(MediaType.APPLICATION_JSON_TYPE)
                      .build());
    }
}
