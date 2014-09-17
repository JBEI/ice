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

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.net.RemoteAccessController;

/**
 * REST resource for sending/retrieving messages from remote
 * ICE instances. Local instances accesses this resource which contacts the remote
 * resource on it's behalf
 *
 * @author Hector Plahar
 */
@Path("/remote/{id}")
public class RemoteAccessResource extends RestResource {

    private RemoteAccessController controller = new RemoteAccessController();

    /**
     * @param remoteId        unique identifier for remote partner being accessed
     * @param userAgentHeader session id (todo : accept api key)
     * @return list of available folders that are available on the registry
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/available")
    public ArrayList<FolderDetails> readRemoteUser(@PathParam("id") long remoteId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return controller.getAvailableFolders(remoteId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/entries")
    public FolderDetails getPublicEntries(
            @PathParam("id") long remoteId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("created") @QueryParam("sort") String sort,
            @DefaultValue("false") @QueryParam("asc") boolean asc,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return controller.getPublicEntries(remoteId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/{email}")
    public AccountTransfer getRemoteUser(@PathParam("id") long remoteId,
            @PathParam("email") String email,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return controller.getRemoteUser(remoteId, email);
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
        return controller.getPublicFolderEntries(remoteId, folderId);
    }
}
