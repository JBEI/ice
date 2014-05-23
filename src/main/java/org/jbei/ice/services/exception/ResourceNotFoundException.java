package org.jbei.ice.services.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Hector Plahar
 */
public class ResourceNotFoundException extends WebApplicationException {

    public ResourceNotFoundException() {
        super(Response.Status.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
//        super(Response.status(Responses.NOT_FOUND).
//                entity(message).type("text/plain").build());
    }
}
