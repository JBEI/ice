package org.jbei.ice.services.rest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.access.AuthorizationException;
import org.jbei.ice.lib.bulkupload.BulkEntryCreator;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.bulkupload.FileBulkUpload;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.utils.Utils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Rest API for interacting with Bulk upload resources
 *
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
                               @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);

        try {
            Logger.info(userId + ": retrieving bulk import with id \"" + id + "\" [" + offset + ", " + limit + "]");
            return controller.getBulkImport(userId, id, offset, limit);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves matching part numbers to be linked to entries in a bulk upload
     *
     * @return list of matching part numbers based on passed parameters
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/partNumbers")
    public Response getPartNumbersForUpload(
            @QueryParam("type") EntryType uploadType,
            @QueryParam("token") String token,
            @DefaultValue("8") @QueryParam("limit") int limit,
            @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        getUserIdFromSessionHeader(sessionId);
        ArrayList<String> results = controller.getMatchingPartNumbersForLinks(uploadType, token, limit);
        return super.respond(results);
    }

    /**
     * Retrieves permissions associated with an upload
     *
     * @param sessionId unique session identifier for the user making request
     * @param id        unique identifier for the upload
     * @return retrieved permissions for specified upload if user has required permissions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public Response getUploadPermissions(@PathParam("id") long id,
                                         @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        List<AccessPermission> permissionList = controller.getUploadPermissions(userId, id);
        return super.respond(permissionList);
    }

    /**
     * Add upload permission
     *
     * @param sessionId unique session identifier for the user making request
     * @param id        unique identifier for the upload
     */
    @POST
    @Path("/{id}/permissions")
    public Response addPermission(@PathParam("id") long id,
                                  @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
                                  AccessPermission accessPermission) {
        String userId = getUserIdFromSessionHeader(sessionId);
        AccessPermission permission = controller.addPermission(userId, id, accessPermission);
        return super.respond(permission);
    }

    @DELETE
    @Path("/{id}/permissions/{pid}")
    public Response removePermission(@PathParam("id") long id,
                                     @PathParam("pid") long permissionId,
                                     @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        boolean success = controller.deletePermission(userId, id, permissionId);
        return super.respond(success);
    }

    @POST
    @Path("/{id}/sequence")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadSequenceFile(@PathParam("id") long uploadId,
                                       @FormDataParam("file") InputStream fileInputStream,
                                       @FormDataParam("entryId") long entryId,
                                       @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
                                       @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String fileName = contentDispositionHeader.getFileName();
            String userId = super.getUserIdFromSessionHeader(sessionId);
            String sequence = IOUtils.toString(fileInputStream);
            SequenceInfo sequenceInfo = controller.addSequence(userId, uploadId, entryId, sequence, fileName);
            if (sequenceInfo == null)
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.OK).entity(sequenceInfo).build();
        } catch (Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{id}/attachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadAttachmentFile(@PathParam("id") long uploadId,
                                         @FormDataParam("file") InputStream fileInputStream,
                                         @FormDataParam("entryId") long entryId,
                                         @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
                                         @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String fileName = contentDispositionHeader.getFileName();
            String userId = super.getUserIdFromSessionHeader(sessionId);
            AttachmentInfo attachmentInfo = controller.addAttachment(userId, uploadId, entryId, fileInputStream,
                    fileName);
            if (attachmentInfo == null)
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.OK).entity(attachmentInfo).build();
        } catch (Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates or updates a list of entries that are to be associated with the specified bulk upload
     *
     * @return wrapper around list of created or updated entries
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public BulkUploadInfo updateList(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
                                     @PathParam("id") long id, BulkUploadInfo info) {
        String userId = getUserIdFromSessionHeader(sessionId);
        return creator.createOrUpdateEntries(userId, id, info.getEntryList());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pending")
    public Response getPendingUploads(@HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String userId = getUserIdFromSessionHeader(sessionId);
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
    public Response updateStatus(
            @PathParam("id") long id,
            BulkUploadInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        Logger.info(userId + ": updating bulk upload status for " + info.getId() + " to " + info.getStatus());
        BulkUploadInfo resp = creator.updateStatus(userId, id, info.getStatus());
        if (resp == null)
            return super.respond(Response.Status.BAD_REQUEST);
        return super.respond(resp);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry")
    public Response createEntry(@PathParam("id") long uploadId,
                                PartData data,
                                @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String userId = getUserIdFromSessionHeader(sessionId);
            Logger.info(userId + ": adding entry to upload \"" + uploadId + "\"");
            PartData result = creator.createEntry(userId, uploadId, data);
            return respond(result);
        } catch (Exception e) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}")
    public Response updateEntry(@PathParam("id") long uploadId,
                                @PathParam("entryId") long entryId,
                                PartData data,
                                @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String userId = getUserIdFromSessionHeader(sessionId);
            Logger.info(userId + ": updating entry \"" + entryId + "\" for upload \"" + uploadId + "\"");
            PartData result = creator.updateEntry(userId, uploadId, entryId, data);
            return respond(result);
        } catch (Exception e) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
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
            // converted to string because there is no messagebodywriter for json for long
            String importId = Long.toString(bulkUpload.process());
            return Response.status(Response.Status.OK).entity(importId).build();
        } catch (IOException e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}")
    public Response deleteEntry(@PathParam("id") long uploadId,
                                @PathParam("entryId") long entryId,
                                @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String userId = getUserIdFromSessionHeader(sessionId);
            if (controller.deleteEntry(userId, uploadId, entryId))
                return Response.ok().build();
            return Response.serverError().build();
        } catch (Exception e) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}/sequence")
    public Response deleteEntrySequence(@PathParam("id") long uploadId,
                                        @PathParam("entryId") long entryId,
                                        @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String userId = getUserIdFromSessionHeader(sessionId);
            if (new SequenceController().deleteSequence(userId, entryId))
                return Response.ok().build();
            return Response.serverError().build();
        } catch (Exception e) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}/attachment")
    public Response deleteEntryAttachment(@PathParam("id") long uploadId,
                                          @PathParam("entryId") long entryId,
                                          @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String userId = getUserIdFromSessionHeader(sessionId);
            if (controller.deleteAttachment(userId, uploadId, entryId))
                return Response.ok().build();
            return Response.serverError().build();
        } catch (Exception e) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
