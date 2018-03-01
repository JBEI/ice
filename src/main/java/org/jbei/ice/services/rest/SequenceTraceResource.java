package org.jbei.ice.services.rest;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.entry.sequence.TraceSequences;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

/**
 * Rest resource support for trace sequences
 *
 * @author Hector Plahar
 */
@Path("/traces")
public class SequenceTraceResource extends RestResource {

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSequences(@FormDataParam("file") InputStream fileInputStream,
                                    @FormDataParam("file") FormDataContentDisposition contentDisposition) {
        String userId = requireUserId();
        TraceSequences sequences = new TraceSequences(userId);
        List<String> errors = sequences.bulkUpdate(fileInputStream);
        return super.respond(errors);
    }
}
