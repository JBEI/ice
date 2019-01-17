package org.jbei.ice.services.rest;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.entry.sequence.Sequences;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

/**
 * REST resource for sequences
 *
 * @author Hector Plahar
 */
@Path("/sequences")
public class SequenceResource extends RestResource {

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSequences(@FormDataParam("file") InputStream fileInputStream,
                                    @FormDataParam("file") FormDataContentDisposition contentDisposition) {
        String userId = requireUserId();
        try {
            Sequences controller = new Sequences(userId);
            List<String> errors = controller.bulkUpdate(userId, fileInputStream);
            return super.respond(errors);
        } catch (PermissionException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            Logger.error(e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/history")
    public Response getSequenceHistory(@PathParam("id") String identifier) {
        String userId = requireUserId();
        return super.respond(false);

    }
}
