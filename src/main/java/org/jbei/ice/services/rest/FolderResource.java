package org.jbei.ice.services.rest;

import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.OwnerEntries;
import org.jbei.ice.lib.entry.SharedEntries;
import org.jbei.ice.lib.entry.VisibleEntries;
import org.jbei.ice.lib.folder.FolderContent;
import org.jbei.ice.lib.folder.FolderContentRetriever;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.folder.UserFolder;
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

    /**
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

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFolder(@PathParam("id") long folderId) {
        String userId = getUserId();
        UserFolder folder = new UserFolder(userId);
        return super.respond(folder.getFolder(folderId));
    }

    /**
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
     * Adds entries referenced in the <code>entrySelection</code> object
     * to the folders also referenced in the same object
     *
     * @return Response with updated collection details
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSelectedEntriesToFolder(final EntrySelection entrySelection) {
        final String userId = getUserId();
        final FolderContent folderContent = new FolderContent();
        final List<FolderDetails> result = folderContent.addEntrySelection(userId, entrySelection);
        return super.respond(result);
    }

    /**
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
            // ok. just not a number
            Logger.debug("Passed folder id " + folderId + " is not a number");
        }

        final FolderDetails details = new FolderDetails();
        log(userId, "retrieving " + folderId + " entries");

        switch (folderId) {
            case "personal":
                OwnerEntries ownerEntries = new OwnerEntries(userId, userId);
                final List<PartData> entries = ownerEntries.retrieveOwnerEntries(field, asc, offset, limit);
                final long count = ownerEntries.getNumberOfOwnerEntries();
                details.getEntries().addAll(entries);
                details.setCount(count);
                return details;

            case "available":
                VisibleEntries visibleEntries = new VisibleEntries(userId);
                final FolderDetails retrieved = visibleEntries.getEntries(field, asc, offset, limit);
                details.setEntries(retrieved.getEntries());
                details.setCount(visibleEntries.getEntryCount());
                return details;

            case "shared":
                SharedEntries sharedEntries = new SharedEntries(userId);
                final List<PartData> data = sharedEntries.getEntries(field, asc, offset, limit);
                details.setEntries(data);
                details.setCount(sharedEntries.getNumberofEntries());
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
