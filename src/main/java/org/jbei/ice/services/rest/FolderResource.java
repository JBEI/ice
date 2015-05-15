package org.jbei.ice.services.rest;

import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.folder.*;
import org.jbei.ice.lib.shared.ColumnField;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
@Path("/folders")
public class FolderResource extends RestResource {

    private FolderController controller = new FolderController();
    private FolderContentRetriever retriever = new FolderContentRetriever();
    private PermissionsController permissionsController = new PermissionsController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection retrieveCollection(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String sid = getUserIdFromSessionHeader(userAgentHeader);
        return controller.getFolderStats(sid);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDetails create(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            FolderDetails folder) {
        String sid = getUserIdFromSessionHeader(userAgentHeader);
        return controller.createPersonalFolder(sid, folder);
    }

    // TODO : allow api key as well
    @GET
    @Path("/public")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> getPublicFolders(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return controller.getPublicFolders();
    }

    @GET
    @Path("/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> getSubFolders(
            @DefaultValue("personal") @PathParam("type") String folderType,
            @DefaultValue("false") @QueryParam("canEdit") boolean canEdit,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String sid = getUserIdFromSessionHeader(userAgentHeader);
        if (canEdit)
            return new Folders().getCanEditFolders(sid);

        switch (folderType) {
            case "personal":
                return controller.getUserFolders(sid);

            case "available":
                return controller.getAvailableFolders(sid);

            case "drafts":
                return controller.getBulkUploadDrafts(sid);

            case "pending":
                return controller.getPendingBulkUploads(sid);

            case "shared":
                return controller.getSharedUserFolders(sid);

            default:
                return new ArrayList<>();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") long folderId,
                           FolderDetails details,
                           @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        FolderDetails resp = controller.update(userId, folderId, details);
        return super.respond(Response.Status.OK, resp);
    }

    @DELETE
    @Path("/{id}")
    public FolderDetails deleteFolder(@PathParam("id") long folderId,
                                      @QueryParam("type") String folderType,
                                      @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        FolderType type = FolderType.valueOf(folderType);
        return controller.delete(userId, folderId, type);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transfer")
    public Response addSelectedEntriesToFolder(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
            EntrySelection entrySelection) {
        String userId = getUserIdFromSessionHeader(sessionId);
        FolderContent folderContent = new FolderContent();
        List<FolderDetails> result = folderContent.addEntrySelection(userId, entrySelection);
        return super.respond(result);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response removeEntriesFromFolder(
            EntrySelection entrySelection,
            @PathParam("id") long folderId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        if (controller.removeFolderContents(userId, folderId, entrySelection))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public FolderDetails read(@Context UriInfo uriInfo,
                              @PathParam("id") String folderId,
                              @DefaultValue("0") @QueryParam("offset") int offset,
                              @DefaultValue("15") @QueryParam("limit") int limit,
                              @DefaultValue("created") @QueryParam("sort") String sort,
                              @DefaultValue("false") @QueryParam("asc") boolean asc,
                              @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {

        ColumnField field = ColumnField.valueOf(sort.toUpperCase());

        if (folderId.equalsIgnoreCase("public")) {
            // return public entries
            log(uriInfo.getBaseUri().toString(), "requesting public entries");
            return controller.getPublicEntries(field, offset, limit, asc);
        }

        String userId = SessionHandler.getUserIdBySession(userAgentHeader);

        try {
            long id = Long.decode(folderId);
            Logger.info("Retrieving folder " + id + " entries");
            return controller.retrieveFolderContents(userId, id, field, asc, offset, limit);
        } catch (NumberFormatException nfe) {
        }

        EntryController entryController = new EntryController();
        FolderDetails details = new FolderDetails();
        log(userId, "retrieving " + folderId + " entries");

        switch (folderId) {
            case "personal":
                List<PartData> entries = entryController.retrieveOwnerEntries(userId, userId, false, field,
                        asc, offset, limit);
                long count = entryController.getNumberOfOwnerEntries(userId, userId);
                details.getEntries().addAll(entries);
                details.setCount(count);
                return details;

            case "available":
                FolderDetails retrieved = entryController.retrieveVisibleEntries(userId, field, asc, offset, limit);
                details.setEntries(retrieved.getEntries());
                details.setCount(entryController.getNumberOfVisibleEntries(userId));
                return details;

            case "shared":
                List<PartData> data = entryController.getEntriesSharedWithUser(userId, field, asc, offset, limit);
                details.setEntries(data);
                details.setCount(entryController.getNumberOfEntriesSharedWithUser(userId));
                return details;

            case "drafts":
                return retriever.getDraftEntries(userId, field, asc, offset, limit);

            case "deleted":
                return retriever.getDeletedEntries(userId, field, asc, offset, limit);

            case "pending":
                return retriever.getPendingEntries(userId, field, asc, offset, limit);

            case "transferred":
                return retriever.getTransferredEntries(userId, field, asc, offset, limit);

            default:
                return null;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public Response getFolderPermissions(
            @PathParam("id") long folderId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return respond(controller.getPermissions(userId, folderId));
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public FolderDetails setPermissions(@Context UriInfo info, @PathParam("id") long folderId,
                                        ArrayList<AccessPermission> permissions,
                                        @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return permissionsController.setFolderPermissions(userId, folderId, permissions);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public AccessPermission addPermission(@Context UriInfo info, @PathParam("id") long folderId,
                                          AccessPermission permission,
                                          @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.createFolderPermission(userId, folderId, permission);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/{permissionId}")
    public Response removePermission(@Context UriInfo info,
                                     @PathParam("id") long partId,
                                     @PathParam("permissionId") long permissionId,
                                     @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        permissionsController.removeFolderPermission(userId, partId, permissionId);
        return Response.ok().build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response enablePublicAccess(@Context UriInfo info, @PathParam("id") long folderId,
                                       @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        if (controller.enablePublicReadAccess(userId, folderId))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response disablePublicAccess(@Context UriInfo info, @PathParam("id") long folderId,
                                        @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        if (controller.disablePublicReadAccess(userId, folderId))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
