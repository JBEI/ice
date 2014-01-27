package org.jbei.ice.services.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.shared.ColumnField;

/**
 * @author Hector Plahar
 */
@Path("/folder")
public class FolderResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public FolderDetails read(@Context UriInfo info,
            @PathParam("id") long folderId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("15") @QueryParam("limit") int limit,
            @DefaultValue("created") @QueryParam("sort") String sort) {
        try {
            HibernateHelper.beginTransaction();
            FolderController folderController = new FolderController();
            Account account = DAOFactory.getAccountDAO().get(123);
            ColumnField field = ColumnField.valueOf(sort.toUpperCase());
            return folderController.retrieveFolderContents(account, folderId, field, false, offset, limit);
        } catch (ControllerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            HibernateHelper.commitTransaction();
        }
    }
}
