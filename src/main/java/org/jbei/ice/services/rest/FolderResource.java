package org.jbei.ice.services.rest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderWrapper;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.folder.Collection;
import org.jbei.ice.lib.folder.FolderContentRetriever;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.services.exception.ResourceNotFoundException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
    public FolderWrapper getPublicFolders(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return controller.getPublicFolders();
    }

    @GET
    @Path("/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> getSubFolders(
            @PathParam("type") String folderType,   // todo : default of personal
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String sid = getUserIdFromSessionHeader(userAgentHeader);

        switch (folderType) {
            case "personal":
                return controller.getUserFolders(sid);

            case "available":
                return controller.getAvailableFolders(sid);

            case "bulkUpload":
                return controller.getBulkUploadDrafts(sid);

            case "shared":
                return controller.getSharedUserFolders(sid);

            default:
                return new ArrayList<>();
        }
    }

    @DELETE
    @Path("/{id}")
    public FolderDetails deleteFolder(@PathParam("id") long folderId,
            @QueryParam("type") String folderType,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.delete(userId, folderId, folderType);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> addEntriesToFolders(ArrayList<FolderDetails> list,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        Type fooType = new TypeToken<ArrayList<FolderDetails>>() {
        }.getType();
        Gson gson = new GsonBuilder().create();
        ArrayList<FolderDetails> data = gson.fromJson(gson.toJsonTree(list), fooType);
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.addEntriesToFolder(userId, data);
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

        try {
            FolderController folderController = new FolderController();
            String userId = SessionHandler.getUserIdBySession(userAgentHeader);
            ColumnField field = ColumnField.valueOf(sort.toUpperCase());

            try {
                long id = Long.decode(folderId);
                return folderController.retrieveFolderContents(userId, id, field, asc, offset, limit);
            } catch (NumberFormatException nfe) {
            }

            EntryController entryController = new EntryController();
            FolderDetails details = new FolderDetails();
            switch (folderId) {
                case "personal":
                    List<PartData> entries = entryController.retrieveOwnerEntries(userId, userId, field,
                                                                                  asc, offset, limit);
                    long count = entryController.getNumberOfOwnerEntries(userId, userId);
                    details.getEntries().addAll(entries);
                    details.setCount(count);
                    return details;

                case "available":
                    try {
                        Logger.info("Retrieving " + folderId + " entries");
                        FolderDetails retrieved = entryController.retrieveVisibleEntries(userId, field, asc, offset,
                                                                                         limit);
                        details.setEntries(retrieved.getEntries());
                        details.setCount(entryController.getNumberOfVisibleEntries(userId));
                        return details;
                    } catch (ControllerException e) {
                        Logger.error(e);
                        return null;
                    }

                case "shared":
                    List<PartData> data = entryController.getEntriesSharedWithUser(userId, field, asc, offset, limit);
                    details.setEntries(data);
                    details.setCount(entryController.getNumberofEntriesSharedWithUser(userId));
                    return details;

                case "bulkUpload":
                    return retriever.getDraftEntries(userId, field, asc, offset, limit);

                case "deleted":
                    return retriever.getDeletedEntries(userId, field, asc, offset, limit);

                default:
                    return null;
            }
        } catch (ControllerException e) {
            throw new ResourceNotFoundException();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public ArrayList<AccessPermission> getFolderPermissions(
            @PathParam("id") long folderId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return permissionsController.getSetFolderPermissions(userId, folderId);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public PartData setPermissions(@Context UriInfo info, @PathParam("id") long partId,
            ArrayList<AccessPermission> permissions,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return permissionsController.setEntryPermissions(userId, partId, permissions);
    }
}
