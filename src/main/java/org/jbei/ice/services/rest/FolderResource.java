package org.jbei.ice.services.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.folder.Collection;
import org.jbei.ice.lib.folder.FolderContent;
import org.jbei.ice.lib.folder.FolderContentRetriever;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.shared.ColumnField;

/**
 * @author Hector Plahar
 */
@Path("/folders")
public class FolderResource extends RestResource {

    private FolderController controller = new FolderController();
    private FolderContentRetriever retriever = new FolderContentRetriever();
    private PermissionsController permissionsController = new PermissionsController();

    /**
     * @return all collections visible to current user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection retrieveCollection() {
        final String sid = getUserId();
        return controller.getFolderStats(sid);
    }

    /**
     * @param folder
     * @return Response with info on created folder
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public FolderDetails create(final FolderDetails folder) {
        final String sid = getUserId();
        return controller.createPersonalFolder(sid, folder);
    }

    /**
     * TODO allow api key as well
     *
     * @return all public collections
     */
    @GET
    @Path("/public")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> getPublicFolders() {
        return controller.getPublicFolders();
    }

    /**
     * @param folderType
     * @return all collections of a type
     */
    @GET
    @Path("/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> getSubFolders(
            @DefaultValue("personal") @PathParam("type") final String folderType) {
        final String sid = getUserId();

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

    /**
     * @param folderId
     * @param details
     * @return Response with updated collection info
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") final long folderId, final FolderDetails details) {
        final String userId = getUserId();
        final FolderDetails resp = controller.update(userId, folderId, details);
        return super.respond(Response.Status.OK, resp);
    }

    /**
     * @param folderId
     * @param folderType
     * @return the details of the deleted collection
     */
    @DELETE
    @Path("/{id}")
    public FolderDetails deleteFolder(@PathParam("id") final long folderId,
            @QueryParam("type") final String folderType) {
        final String userId = getUserId();
        final FolderType type = FolderType.valueOf(folderType);
        return controller.delete(userId, folderId, type);
    }

    /**
     * @param entrySelection
     * @return Response with updated collection details
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transfer")
    public Response addSelectedEntriesToFolder(final EntrySelection entrySelection) {
        final String userId = getUserId();
        final FolderContent folderContent = new FolderContent();
        final List<FolderDetails> result = folderContent.addEntrySelection(userId, entrySelection);
        return super.respond(result);
    }

    /**
     * @param entrySelection
     * @param folderId
     * @return Response indicating success or failure
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response removeEntriesFromFolder(final EntrySelection entrySelection,
            @PathParam("id") final long folderId) {
        final String userId = getUserId();
        if (controller.removeFolderContents(userId, folderId, entrySelection)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param uriInfo
     * @param folderId
     * @param offset
     * @param limit
     * @param sort
     * @param asc
     * @return details of the selected collection
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public FolderDetails read(@Context final UriInfo uriInfo,
            @PathParam("id") final String folderId,
            @DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit,
            @DefaultValue("created") @QueryParam("sort") final String sort,
            @DefaultValue("false") @QueryParam("asc") final boolean asc) {

        final ColumnField field = ColumnField.valueOf(sort.toUpperCase());

        if (folderId.equalsIgnoreCase("public")) {
            // return public entries
            log(uriInfo.getBaseUri().toString(), "requesting public entries");
            return controller.getPublicEntries(field, offset, limit, asc);
        }

        final String userId = getUserId();

        try {
            final long id = Long.decode(folderId);
            Logger.info("Retrieving folder " + id + " entries");
            return controller.retrieveFolderContents(userId, id, field, asc, offset, limit);
        } catch (final NumberFormatException nfe) {
        }

        final EntryController entryController = new EntryController();
        final FolderDetails details = new FolderDetails();
        log(userId, "retrieving " + folderId + " entries");

        switch (folderId) {
        case "personal":
            final List<PartData> entries = entryController.retrieveOwnerEntries(userId, userId,
                    field, asc, offset, limit);
            final long count = entryController.getNumberOfOwnerEntries(userId, userId);
            details.getEntries().addAll(entries);
            details.setCount(count);
            return details;

        case "available":
            final FolderDetails retrieved = entryController.retrieveVisibleEntries(userId, field,
                    asc, offset, limit);
            details.setEntries(retrieved.getEntries());
            details.setCount(entryController.getNumberOfVisibleEntries(userId));
            return details;

        case "shared":
            final List<PartData> data = entryController.getEntriesSharedWithUser(userId, field,
                    asc, offset, limit);
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

    /**
     * @param folderId
     * @return Response with permissions on a collection
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public Response getFolderPermissions(@PathParam("id") final long folderId) {
        final String userId = getUserId();
        return respond(controller.getPermissions(userId, folderId));
    }

    /**
     * @param info
     * @param folderId
     * @param permissions
     * @return details of the modified collection
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public FolderDetails setPermissions(@Context final UriInfo info,
            @PathParam("id") final long folderId, final ArrayList<AccessPermission> permissions) {
        final String userId = getUserId();
        return permissionsController.setFolderPermissions(userId, folderId, permissions);
    }

    /**
     * @param info
     * @param folderId
     * @param permission
     * @return the added permission
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public AccessPermission addPermission(@Context final UriInfo info,
            @PathParam("id") final long folderId, final AccessPermission permission) {
        final String userId = getUserId();
        return controller.createFolderPermission(userId, folderId, permission);
    }

    /**
     * @param info
     * @param partId
     * @param permissionId
     * @return Response for success or failure
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/{permissionId}")
    public Response removePermission(@Context final UriInfo info,
            @PathParam("id") final long partId, @PathParam("permissionId") final long permissionId) {
        final String userId = getUserId();
        permissionsController.removeFolderPermission(userId, partId, permissionId);
        return Response.ok().build();
    }

    /**
     * @param info
     * @param folderId
     * @return Response for success or failure
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response enablePublicAccess(@Context final UriInfo info,
            @PathParam("id") final long folderId) {
        final String userId = getUserId();
        if (controller.enablePublicReadAccess(userId, folderId)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param info
     * @param folderId
     * @return Response for success or failure
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response disablePublicAccess(@Context final UriInfo info,
            @PathParam("id") final long folderId) {
        final String userId = getUserId();
        if (controller.disablePublicReadAccess(userId, folderId)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
