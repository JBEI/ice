package org.jbei.ice.services.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Collection;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.services.exception.ResourceNotFoundException;
import org.jbei.ice.servlet.ModelToInfoFactory;

/**
 * @author Hector Plahar
 */
@Path("/folders")
public class FolderResource extends RestResource {

    private FolderController controller = new FolderController();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection retrieveCollection(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        try {
            HibernateHelper.beginTransaction();
            String sid = getUserIdFromSessionHeader(userAgentHeader);
            return controller.getFolderStats(sid);
        } finally {
            HibernateHelper.commitTransaction();
        }
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> getAllFolders(
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        try {
            HibernateHelper.beginTransaction();
            String sid = getUserIdFromSessionHeader(userAgentHeader);
            return controller.retrieveFoldersForUser(sid);
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        } finally {
            HibernateHelper.commitTransaction();
        }
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<FolderDetails> getUserFolders(@HeaderParam(value = "X-ICE-Authentication-SessionId")
    String userAgentHeader) {
        try {
            HibernateHelper.beginTransaction();
            String sid = getUserIdFromSessionHeader(userAgentHeader);
            return controller.getUserFolders(sid);
        } finally {
            HibernateHelper.commitTransaction();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public FolderDetails read(@Context UriInfo uriInfo,
            @PathParam("id") String folderId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("created") @QueryParam("sort") String sort,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        try {
            HibernateHelper.beginTransaction();
            FolderController folderController = new FolderController();
            String userId = getUserIdFromSessionHeader(userAgentHeader);
            ColumnField field = ColumnField.valueOf(sort.toUpperCase());

            try {
                long id = Long.decode(folderId);
                return folderController.retrieveFolderContents(userId, id, field, false, offset, limit);
            } catch (NumberFormatException nfe) {
            }

            switch (folderId) {
                case "personal":
                    EntryController entryController = new EntryController();
                    FolderDetails details = new FolderDetails();
                    details.setType(FolderType.SHARED);
                    Account account = DAOFactory.getAccountDAO().getByEmail(userId);
                    List<Entry> entries = entryController.retrieveOwnerEntries(account, userId, null,
                                                                               true, offset, limit);
                    long count = entryController.getNumberOfOwnerEntries(account, userId);
                    details.setCount(count);
                    for (Entry entry : entries) {
                        PartData info = ModelToInfoFactory.createTableViewData(entry, false);
//                        try {
//                            info.setCanEdit(permissionsController.acc(account, entry));
//                        } catch (ControllerException ce) {
//                            continue;
//                        }
                        details.getEntries().add(info);
                    }
                    return details;

                default:
                    return null;
            }
        } catch (ControllerException e) {
            throw new ResourceNotFoundException();
        } finally {
            HibernateHelper.commitTransaction();
        }
    }
}
