package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.net.RemoteAccessController;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

/**
 * REST resource for sending/retrieving messages from remote
 * ICE instances. Local instances access this resource which contacts the remote
 * resource on its behalf
 *
 * @author Hector Plahar
 */
@Path("/remote/{id}")
public class RemoteAccessResource extends RestResource {

    private RemoteAccessController controller = new RemoteAccessController();

    /**
     * @param remoteId unique identifier for remote partner being accessed
     * @return list of available folders that are available on the registry
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/available")
    public ArrayList<FolderDetails> readRemoteUser(@PathParam("id") long remoteId) {
        return controller.getAvailableFolders(remoteId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/{email}")
    public AccountTransfer getRemoteUser(@PathParam("id") long remoteId,
            @PathParam("email") String email) {
        return controller.getRemoteUser(remoteId, email);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{entryId}/sequence")
    public Response getSequence(@PathParam("id") long remoteId,
            @PathParam("entryId") long partId) {
        FeaturedDNASequence sequence = controller.getRemoteSequence(remoteId, partId);
        if (sequence == null)
            return Response.status(Response.Status.NO_CONTENT).build();
        return Response.status(Response.Status.OK).entity(sequence).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/folders/{folderId}")
    public FolderDetails getPublicFolderEntries(
            @PathParam("id") long remoteId,
            @PathParam("folderId") long folderId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("created") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return controller.getPublicFolderEntries(remoteId, folderId, sort, asc, offset, limit);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parts/{partId}/samples")
    public Response getRemotePartSamples(@PathParam("id") long remoteId,
            @PathParam("partId") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        ArrayList<PartSample> result = controller.getRemotePartSamples(remoteId, partId);
        return super.respond(result);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parts/{partId}/comments")
    public Response getRemotePartComments(@PathParam("id") long remoteId,
            @PathParam("partId") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        ArrayList<UserComment> result = controller.getRemotePartComments(remoteId, partId);
        return super.respond(result);
    }
}
