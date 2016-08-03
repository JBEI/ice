package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.common.PageParameters;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.folder.*;
import org.jbei.ice.lib.shared.ColumnField;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Rest resource for dealing with folders. Note that this is different from collections
 * whose api can be found in the {@link CollectionResource} class.
 * <br>
 * Folders are generally contained in collections and can be created and deleted in an ad-hoc manner
 * while collections are a system defined fixed set.
 *
 * @author Hector Plahar
 */
@Path("/folders")
public class FolderResource extends RestResource {

    private FolderController controller = new FolderController();
    private PermissionsController permissionsController = new PermissionsController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete")
    public Response getAutoCompleteForAvailableFolders(
            @QueryParam("val") final String val,
            @DefaultValue("8") @QueryParam("limit") final int limit) {
        final String userId = requireUserId();
        Folders folders = new Folders(userId);
        return super.respond(folders.filter(val, limit));
    }

    @GET
    public Response getFolders() {
        String userId = requireUserId();
        log(userId, "retrieving folders");
        Folders folders = new Folders(userId);
        return super.respond(folders.getCanEditFolders());
    }

    /**
     * Creates a new folder with the details specified in the parameter. The folder
     * is either created by a user or represents one that is transferred
     * <p>
     * The default type for the folder is <code>PRIVATE</code> and is owned by the user creating it
     * unless it is transferred
     *
     * @param isTransfer whether the folder being created is a transfer folder or not
     * @param folder     details of the folder to create
     * @return information about the created folder including the unique identifier
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@DefaultValue("false") @QueryParam("isTransfer") boolean isTransfer,
                           final FolderDetails folder) {
        FolderDetails created;
        if (!isTransfer) {
            final String userId = requireUserId();
            log(userId, "creating new folder");
            created = controller.createPersonalFolder(userId, folder);
        } else {
            // todo : get partner identifier
            created = controller.createTransferredFolder(folder);
        }

        return super.respond(created);
    }

    @GET
    @Path("/public")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> getPublicFolders() {
        return controller.getPublicFolders();
    }

    /**
     * Retrieve specified folder resource
     *
     * @param folderId unique folder resource identifier
     * @return folder specified by resource id, if the user making request has appropriate privileges
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFolder(@PathParam("id") long folderId) {
        String userId = requireUserId();
        log(userId, "get folder \"" + folderId + "\"");
        try {
            UserFolder folder = new UserFolder(userId);
            return super.respond(folder.getFolder(folderId));
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    /**
     * Updates the specified folder resource
     *
     * @param folderId resource identifier of folder to be updated
     * @param details  details for update
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") final long folderId,
                           final FolderDetails details) {
        final String userId = requireUserId();
        log(userId, "update folder \"" + folderId + "\"");
        final FolderDetails resp = controller.update(userId, folderId, details);
        return super.respond(Response.Status.OK, resp);
    }

    /**
     * Deletes the specified folder resource
     *
     * @return the details of the deleted collection
     */
    @DELETE
    @Path("/{id}")
    public FolderDetails deleteFolder(@PathParam("id") final long folderId,
                                      @QueryParam("type") final String folderType) {
        final String userId = getUserId();
        final FolderType type = FolderType.valueOf(folderType);
        log(userId, "deleting " + type + " folder " + folderId);
        return controller.delete(userId, folderId, type);
    }

    /**
     * Adds contents referenced in the <code>entrySelection</code> object
     * to the folders also referenced in the same object
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/entries")
    public Response addSelectedEntriesToFolders(final EntrySelection entrySelection,
                                                @QueryParam("token") String remoteUserToken,
                                                @QueryParam("userId") String remoteUserId,
                                                @QueryParam("folderId") long fid) {
        final String userId = getUserId();
        final FolderContents folderContents = new FolderContents();
        if (StringUtils.isEmpty(userId) && !StringUtils.isEmpty(remoteUserToken)) {
            // check others
            log(remoteUserId, " remotely adding entries to folders");
            RegistryPartner registryPartner = requireWebPartner();
            return super.respond(folderContents.remotelyAddEntrySelection(remoteUserId, fid, remoteUserToken,
                    entrySelection, registryPartner));
        } else {
            log(userId, "adding entries to folders");
            folderContents.addEntrySelection(userId, entrySelection);
            return super.respond(true);
        }
    }

    /**
     * Modifies the contents of a folder either by removing or moving entries as determined by the <code>move</code>
     * parameter
     *
     * @param folderId       resource identifier for folder whose contents are to be modified
     * @param move           whether to move the specified entries or simply remove them from the folder
     * @param entrySelection wrapper around context for modification
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response modifyFolderEntries(@PathParam("id") long folderId,
                                        @DefaultValue("false") @QueryParam("move") boolean move,
                                        EntrySelection entrySelection) {
        String userId = requireUserId();
        super.log(userId, "modifying entries for folder " + folderId);
        FolderContents folderContents = new FolderContents();
        boolean success = folderContents.removeFolderContents(userId, folderId, entrySelection, move);
        return super.respond(success);
    }

    /**
     * Retrieves the entries for specified folder. Handles request
     * from a local client (ui) or from a remote ice instance
     *
     * @return list of retrieved entries wrapped in folder object
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public FolderDetails read(@PathParam("id") final String folderId,
                              @DefaultValue("0") @QueryParam("offset") final int offset,
                              @DefaultValue("15") @QueryParam("limit") final int limit,
                              @DefaultValue("created") @QueryParam("sort") final String sort,
                              @DefaultValue("false") @QueryParam("asc") final boolean asc,
                              @DefaultValue("") @QueryParam("filter") String filter,
                              @QueryParam("token") String token,   // todo: move to headers
                              @QueryParam("userId") String remoteUserId,                   // todo : ditto
                              @QueryParam("fields") List<String> queryParam) {
        final ColumnField field = ColumnField.valueOf(sort.toUpperCase());
        if (folderId.equalsIgnoreCase("public")) {   // todo : move to separate rest resource path
            RegistryPartner registryPartner = requireWebPartner();
            // return public entries
            log(registryPartner.getUrl(), "requesting public entries");
            return this.controller.getPublicEntries(field, offset, limit, asc);
        }

        // userId can be empty for public folders
        String userId = super.getUserId();
        try {
            final long id = Long.decode(folderId);
            String message = "retrieving folder " + id + " entries";
            if (filter.length() > 0)
                message += " filtered by \"" + filter + "\"";
            FolderContents folderContents = new FolderContents();
            PageParameters pageParameters = new PageParameters(offset, limit, field, asc, filter);

            if (StringUtils.isEmpty(userId)) {
                if (StringUtils.isEmpty(token))  // todo :verify partner?
                    return folderContents.getContents(userId, id, pageParameters);

                // get registry partner
                RegistryPartner partner = requireWebPartner();
                log(partner.getUrl(), message);
                return folderContents.getRemotelySharedContents(remoteUserId, token, partner, id, pageParameters);
            } else {
                log(userId, message);
                return folderContents.getContents(userId, id, pageParameters);
            }
        } catch (final NumberFormatException nfe) {
            Logger.error("Passed folder id " + folderId + " is not a number");
            return null;
        }
    }

    /**
     * Retrieves list of permissions available for the specified folder. This action
     * is restricted to users who have write privileges on the folder
     *
     * @param folderId unique local folder identifier
     * @return list of permissions for folder
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public Response getFolderPermissions(@PathParam("id") final long folderId) {
        final String userId = requireUserId();
        FolderPermissions folderPermissions = new FolderPermissions(userId, folderId);
        return respond(folderPermissions.get());
    }

    /**
     * @return details of the modified collection
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public FolderDetails setPermissions(@PathParam("id") final long folderId,
                                        final ArrayList<AccessPermission> permissions) {
        final String userId = getUserId();
        return permissionsController.setFolderPermissions(userId, folderId, permissions);
    }

    /**
     * Add new permission to list of permissions for specified folder
     *
     * @param folderId   unique local folder identifier
     * @param permission details about access privilege for folder
     * @return details about access privilege for folder with a local unique identifier
     * which can be used to remove the privilege
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public Response addPermission(@PathParam("id") final long folderId,
                                  final AccessPermission permission) {
        try {
            final String userId = requireUserId();
            FolderPermissions folderPermissions = new FolderPermissions(userId, folderId);
            return super.respond(folderPermissions.createPermission(permission));
        } catch (PermissionException pe) {
            return super.respond(Response.Status.FORBIDDEN);
        } catch (IllegalArgumentException ile) {
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/{permissionId}")
    public Response removePermission(@PathParam("id") long folderId,
                                     @PathParam("permissionId") long permissionId) {
        final String userId = requireUserId();
        FolderPermissions folderPermissions = new FolderPermissions(userId, folderId);
        boolean success = folderPermissions.remove(permissionId);
        return super.respond(success);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response enablePublicAccess(@PathParam("id") final long folderId) {
        final String userId = requireUserId();
        log(userId, "adding public read access to folder " + folderId);
        FolderPermissions folderPermissions = new FolderPermissions(userId, folderId);
        try {
            folderPermissions.enablePublicReadAccess();
            return Response.ok().build();
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response disablePublicAccess(@PathParam("id") final long folderId) {
        final String userId = requireUserId();
        log(userId, "removing public read access from folder " + folderId);
        FolderPermissions folderPermissions = new FolderPermissions(userId, folderId);
        try {
            folderPermissions.disablePublicReadAccess();
            return Response.ok().build();
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }
}
