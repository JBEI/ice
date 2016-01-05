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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
@Path("/uploads")
public class BulkUploadResource extends RestResource {

    private BulkUploadController controller = new BulkUploadController();
    private BulkEntryCreator creator = new BulkEntryCreator();

    /**
     * Retrieves specified bulk upload resource including
     * @param id
     * @param offset
     * @param limit
     * @return model object for bulk upload
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public BulkUploadInfo read(@PathParam("id") final long id,
                               @DefaultValue("0") @QueryParam("offset") final int offset,
                               @DefaultValue("50") @QueryParam("limit") final int limit) {
        final String userId = requireUserId();
        Logger.info(userId + ": retrieving bulk import with id \"" + id + "\"");
        return controller.getBulkImport(userId, id, offset, limit);
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
            @DefaultValue("8") @QueryParam("limit") int limit) {
        requireUserId();
        ArrayList<String> results = controller.getMatchingPartNumbersForLinks(uploadType, token, limit);
        return super.respond(results);
    }

    /**
     * Retrieves permissions associated with an upload
     *
     * @param id unique identifier for the upload
     * @return retrieved permissions for specified upload if user has required permissions
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public Response getUploadPermissions(@PathParam("id") final long id) {
        final String userId = requireUserId();
        final List<AccessPermission> permissionList = controller.getUploadPermissions(userId, id);
        return super.respond(permissionList);
    }

    /**
     * Add upload permission
     *
     * @param id               unique identifier for the upload
     * @param accessPermission model object for permissions applied to upload
     * @return Response with the added permission
     */
    @POST
    @Path("/{id}/permissions")
    public Response addPermission(@PathParam("id") final long id,
                                  final AccessPermission accessPermission) {
        final String userId = requireUserId();
        final AccessPermission permission = controller.addPermission(userId, id, accessPermission);
        return super.respond(permission);
    }

    /**
     * @param id
     * @param permissionId
     * @return Response with success or failure of permissions delete
     */
    @DELETE
    @Path("/{id}/permissions/{pid}")
    public Response removePermission(@PathParam("id") final long id,
                                     @PathParam("pid") final long permissionId) {
        final String userId = requireUserId();
        final boolean success = controller.deletePermission(userId, id, permissionId);
        return super.respond(success);
    }

    /**
     * @param uploadId
     * @param fileInputStream
     * @param entryId
     * @param contentDispositionHeader
     * @return Response with sequence information if upload is successful
     */
    @POST
    @Path("/{id}/sequence")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadSequenceFile(@PathParam("id") final long uploadId,
                                       @FormDataParam("file") final InputStream fileInputStream,
                                       @FormDataParam("entryId") final long entryId,
                                       @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        try {
            final String fileName = contentDispositionHeader.getFileName();
            final String userId = requireUserId();
            final String sequence = IOUtils.toString(fileInputStream);
            final SequenceInfo sequenceInfo = controller.addSequence(userId, uploadId, entryId,
                    sequence, fileName);
            if (sequenceInfo == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.status(Response.Status.OK).entity(sequenceInfo).build();
        } catch (final IOException e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param uploadId
     * @param fileInputStream
     * @param entryId
     * @param contentDispositionHeader
     * @return Response with information on attachment upload
     */
    @POST
    @Path("/{id}/attachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadAttachmentFile(@PathParam("id") final long uploadId,
                                         @FormDataParam("file") final InputStream fileInputStream,
                                         @FormDataParam("entryId") final long entryId,
                                         @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        try {
            final String fileName = contentDispositionHeader.getFileName();
            final String userId = requireUserId();
            final AttachmentInfo attachmentInfo = controller.addAttachment(userId, uploadId,
                    entryId, fileInputStream, fileName);
            if (attachmentInfo == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.status(Response.Status.OK).entity(attachmentInfo).build();
        } catch (final Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates or updates a list of entries that are to be associated with the specified bulk upload
     *
     * @param id
     * @param info
     * @return wrapper around list of created or updated entries
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public BulkUploadInfo updateList(@PathParam("id") final long id,
                                     final BulkUploadInfo info) {
        final String userId = requireUserId();
        return creator.createOrUpdateEntries(userId, id, info.getEntryList());
    }

    /**
     * @return Response with pending upload information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pending")
    public Response getPendingUploads() {
        try {
            final String userId = requireUserId();
            final HashMap<String, ArrayList<BulkUploadInfo>> pending = controller.getPendingImports(userId);
            return Response.status(Response.Status.OK).entity(pending).build();
        } catch (final AuthorizationException ae) {
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param id
     * @param info
     * @return Response with upload information on renamed upload
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/name")
    public Response updateName(@PathParam("id") final long id, final BulkUploadInfo info) {
        final String userId = getUserId();
        Logger.info(userId + ": updating bulk upload name for " + info.getId() + " with value " + info.getName());
        final BulkUploadInfo result = creator.renameBulkUpload(userId, id, info.getName());
        if (result == null) {
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return respond(Response.Status.OK, result);
    }

    /**
     * @param id
     * @param info
     * @return Response with updated bulk upload info
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") final long id, final BulkUploadInfo info) {
        final String userId = getUserId();
        Logger.info(userId + ": updating bulk upload status for " + info.getId() + " to " + info.getStatus());
        final BulkUploadInfo resp = creator.updateStatus(userId, id, info.getStatus());
        if (resp == null) {
            return super.respond(Response.Status.BAD_REQUEST);
        }
        return super.respond(resp);
    }

    /**
     * @param uploadId
     * @param data
     * @return Response with created part data
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry")
    public Response createEntry(@PathParam("id") final long uploadId, final PartData data) {
        final String userId = getUserId();
        Logger.info(userId + ": adding entry to upload \"" + uploadId + "\"");
        final PartData result = creator.createEntry(userId, uploadId, data);
        return respond(result);
    }

    /**
     * @param uploadId
     * @param entryId
     * @param data
     * @return Response with updated part data
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}")
    public Response updateEntry(@PathParam("id") final long uploadId,
                                @PathParam("entryId") final long entryId, final PartData data) {
        final String userId = getUserId();
        Logger.info(userId + ": updating entry \"" + entryId + "\" for upload \"" + uploadId + "\"");
        final PartData result = creator.updateEntry(userId, uploadId, entryId, data);
        return respond(result);
    }

    /**
     * @param info
     * @return a bulk upload info object
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public BulkUploadInfo create(final BulkUploadInfo info) {
        final String userId = requireUserId();
        Logger.info(userId + ": creating bulk upload draft");
        return controller.create(userId, info);
    }

    /**
     * @return all bulk uploads for the current user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<BulkUploadInfo> query() {
        final String userId = getUserId();
        Logger.info(userId + ": retrieving bulk upload drafts");
        return controller.retrieveByUser(userId, userId);
    }

    /**
     * @param fileInputStream
     * @param type
     * @param contentDispositionHeader
     * @return Response with the id of the imported bulk upload
     */
    @POST
    @Path("file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@FormDataParam("file") final InputStream fileInputStream,
                         @FormDataParam("type") final String type,
                         @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        try {
            final String userId = getUserId();
            final String fileName = userId + "-" + contentDispositionHeader.getFileName();
            final File file = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                    "bulk-import", fileName).toFile();
            FileUtils.copyInputStreamToFile(fileInputStream, file);

            final EntryType addType = EntryType.valueOf(type.toUpperCase());
            final FileBulkUpload bulkUpload = new FileBulkUpload(userId, file.toPath(), addType);
            // converted to string because there is no messagebodywriter for json for long
            final String importId = Long.toString(bulkUpload.process());
            return Response.status(Response.Status.OK).entity(importId).build();
        } catch (IOException e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}")
    public Response deleteEntry(@PathParam("id") long uploadId,
                                @PathParam("entryId") long entryId) {
        try {
            final String userId = getUserId();
            if (controller.deleteEntry(userId, uploadId, entryId)) {
                return Response.ok().build();
            }
            return Response.serverError().build();
        } catch (final Exception e) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * @param uploadId
     * @param entryId
     * @return OK response if sequence is deleted
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}/sequence")
    public Response deleteEntrySequence(@PathParam("id") final long uploadId,
                                        @PathParam("entryId") final long entryId) {
        try {
            final String userId = getUserId();
            if (new SequenceController().deleteSequence(userId, entryId)) {
                return Response.ok().build();
            }
            return Response.serverError().build();
        } catch (final Exception e) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * @param uploadId
     * @param entryId
     * @return OK response if attachment is deleted
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}/attachment")
    public Response deleteEntryAttachment(@PathParam("id") final long uploadId,
                                          @PathParam("entryId") final long entryId) {
        try {
            final String userId = getUserId();
            if (controller.deleteAttachment(userId, uploadId, entryId)) {
                return Response.ok().build();
            }
            return Response.serverError().build();
        } catch (final Exception e) {
            return super.respond(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
