package org.jbei.ice.services.rest;

import org.jbei.ice.lib.common.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception mapper for mapping exceptions to {@link Response}s
 *
 * @author Hector Plahar
 */
@Provider
public class ICEExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        Response response;
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            response = webEx.getResponse();
        } else {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (response.getStatus() != Response.Status.UNAUTHORIZED.getStatusCode())
            Logger.error(exception);

        return response;
    }
}
