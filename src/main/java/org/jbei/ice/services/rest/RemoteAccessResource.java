package org.jbei.ice.services.rest;

import java.util.ArrayList;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.net.RemoteAccessController;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

/**
 * REST resource for sending/retrieving messages from remote ICE instances. Local instances access
 * this resource which contacts the remote resource on its behalf
 *
 * @author Hector Plahar
 */
@Path("/remote/{id}")
public class RemoteAccessResource extends RestResource {

    private RemoteAccessController controller = new RemoteAccessController();

    /**
     * @param remoteId
     *            unique identifier for remote partner being accessed
     * @return list of available folders that are available on the registry
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/available")
    public ArrayList<FolderDetails> readRemoteUser(@PathParam("id") final long remoteId) {
        return controller.getAvailableFolders(remoteId);
    }

    /**
     * @param remoteId
     * @param email
     * @return user from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/{email}")
    public AccountTransfer getRemoteUser(@PathParam("id") final long remoteId,
            @PathParam("email") final String email) {
        return controller.getRemoteUser(remoteId, email);
    }

    /**
     * @param remoteId
     * @param partId
     * @return sequence from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{entryId}/sequence")
    public Response getSequence(@PathParam("id") final long remoteId,
            @PathParam("entryId") final long partId) {
        final FeaturedDNASequence sequence = controller.getRemoteSequence(remoteId, partId);
        if (sequence == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.OK).entity(sequence).build();
    }

    /**
     * @param remoteId
     * @param partId
     * @return traces from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parts/{entryId}/traces")
    public Response getSequenceTraces(@PathParam("id") final long remoteId,
            @PathParam("entryId") final long partId) {
        final ArrayList<TraceSequenceAnalysis> traces = controller
                .getRemoteTraces(remoteId, partId);
        if (traces == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.OK).entity(traces).build();
    }

    /**
     * @param remoteId
     * @param folderId
     * @param offset
     * @param limit
     * @param sort
     * @param asc
     * @return public folders from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/folders/{folderId}")
    public FolderDetails getPublicFolderEntries(@PathParam("id") final long remoteId,
            @PathParam("folderId") final long folderId,
            @DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit,
            @DefaultValue("created") @QueryParam("sort") final String sort,
            @DefaultValue("false") @QueryParam("asc") final boolean asc) {
        return controller.getPublicFolderEntries(remoteId, folderId, sort, asc, offset, limit);
    }

    /**
     * @param remoteId
     * @param partId
     * @return part samples from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parts/{partId}/samples")
    public Response getRemotePartSamples(@PathParam("id") final long remoteId,
            @PathParam("partId") final long partId) {
        final ArrayList<PartSample> result = controller.getRemotePartSamples(remoteId, partId);
        return super.respond(result);
    }

    /**
     * @param remoteId
     * @param partId
     * @return comments from remote ICE
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parts/{partId}/comments")
    public Response getRemotePartComments(@PathParam("id") final long remoteId,
            @PathParam("partId") final long partId) {
        final ArrayList<UserComment> result = controller.getRemotePartComments(remoteId, partId);
        return super.respond(result);
    }
}
