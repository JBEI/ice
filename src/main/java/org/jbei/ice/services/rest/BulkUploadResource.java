package org.jbei.ice.services.rest;

import java.util.ArrayList;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.bulkupload.BulkEntryCreator;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.services.exception.UnexpectedException;

/**
 * @author Hector Plahar
 */
@Path("/upload")
public class BulkUploadResource extends RestResource {

    private BulkUploadController controller = new BulkUploadController();
    private BulkEntryCreator creator = new BulkEntryCreator();

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public BulkUploadInfo read(@Context UriInfo info, @PathParam("id") long id,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);

        try {
            Logger.info(userId + ": retrieving bulk import with id \"" + id + "\" [" + offset + ", " + limit + "]");
            return controller.getBulkImport(userId, id, offset, limit);
        } catch (Exception e) {
            return null;
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry")
    public PartData createEntry(@PathParam("id") long uploadId,
            PartData data,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        try {
            String userId = getUserIdFromSessionHeader(userAgentHeader);
            Logger.info(userId + ": adding entry to upload \"" + uploadId + "\"");
            return creator.createEntry(userId, uploadId, data);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}")
    public PartData updateEntry(@PathParam("id") long uploadId,
            @PathParam("entryId") long entryId,
            PartData data,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        try {
            String userId = getUserIdFromSessionHeader(userAgentHeader);
            Logger.info(userId + ": updating entry \"" + entryId + "\" for upload \"" + uploadId + "\"");
            return creator.updateEntry(userId, uploadId, entryId, data);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public BulkUploadInfo create(BulkUploadInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        Logger.info(userId + ": creating bulk upload draft");
        return controller.create(userId, info);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public BulkUploadInfo bulkUpdate(BulkUploadInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        Logger.info(userId + ": updating bulk upload draft " + info.getId());
        return creator.bulkUpdate(userId, info);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<BulkUploadInfo> query(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        Logger.info(userId + ": retrieving bulk upload drafts");
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        try {
            return controller.retrieveByUser(account, account);
        } catch (ControllerException e) {
            throw new UnexpectedException(e.getMessage());
        }
    }
}
