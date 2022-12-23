package org.jbei.ice.services.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.entry.sequence.analysis.TraceSequences;

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
        TraceSequences sequences = new TraceSequences();
        List<String> errors = sequences.bulkUpdate(userId, fileInputStream);
        return super.respond(errors);
    }
}
