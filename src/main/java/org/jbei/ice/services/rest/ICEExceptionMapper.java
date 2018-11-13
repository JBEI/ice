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
 * @author William Morrell
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

        // Handle logging based on response HTTP codes
        final int status = response.getStatus();
        // find where the exception came from
        final StackTraceElement[] traces = exception.getStackTrace();
        final String where;
        if (traces.length > 0) {
        	where = traces[0].toString();
        } else {
        	where = "Unknown trace location";
        }
        final String info = "HTTP " + status + " thrown from " + where;
        // anything 1xx or 2xx using an Exception is weird; log as WARNING
        if (status < 300) {
        	Logger.warn(info);
        }
        // log all 3xx HTTP errors as INFO
        if (300 <= status && status < 400) {
        	Logger.info(info);
        }
        // log all 4xx HTTP errors as WARNING
        else if (400 <= status && status < 500) {
        	Logger.warn(info);
        }
        // log all other HTTP errors as ERROR; generates email by default
        else {
            Logger.error(info, exception);
        }

        return response;
    }
}
