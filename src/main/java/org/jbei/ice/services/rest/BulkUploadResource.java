package org.jbei.ice.services.rest;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.access.AuthorizationException;
import org.jbei.ice.lib.bulkupload.BulkEntryCreator;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.bulkupload.FileBulkUpload;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.exception.UnexpectedException;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * @author Hector Plahar
 */
@Path("/upload")
public class BulkUploadResource extends RestResource {

    private BulkUploadController controller = new BulkUploadController();
    private BulkEntryCreator creator = new BulkEntryCreator();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pending")
    public Response getPendingUploads(@HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        try {
            String userId = getUserIdFromSessionHeader(userAgentHeader);
            HashMap<String, ArrayList<BulkUploadInfo>> pending = controller.getPendingImports(userId);
            return Response.status(Response.Status.OK).entity(pending).build();
        } catch (AuthorizationException ae) {
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/name")
    public Response updateName(@PathParam("id") long id, BulkUploadInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        Logger.info(userId + ": updating bulk upload name for " + info.getId() + " with value " + info.getName());
        BulkUploadInfo result = creator.renameBulkUpload(userId, id, info.getName());
        if (result == null)
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        return respond(Response.Status.OK, result);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/status")
    public BulkUploadInfo updateStatus(
            @PathParam("id") long id,
            BulkUploadInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        Logger.info(userId + ": updating bulk upload status for " + info.getId() + " to " + info.getStatus());
        return creator.updateStatus(userId, id, info.getStatus());
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<BulkUploadInfo> query(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        Logger.info(userId + ": retrieving bulk upload drafts");
        return controller.retrieveByUser(userId, userId);
    }

    @POST
    @Path("file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("type") String type,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String userId = getUserIdFromSessionHeader(sessionId);
            String fileName = userId + "-" + contentDispositionHeader.getFileName();
            File file = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                                  "bulk-import", fileName).toFile();
            FileUtils.copyInputStreamToFile(fileInputStream, file);

            EntryType addType = EntryType.valueOf(type.toUpperCase());
            FileBulkUpload bulkUpload = new FileBulkUpload(userId, file.toPath(), addType);
            // tODO: this returns a string because it is being used to return a message in case of an error
            String importId = bulkUpload.process();
            return Response.status(Response.Status.OK).entity(importId).build();
        } catch (Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
