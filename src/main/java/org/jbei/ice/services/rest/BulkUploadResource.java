package org.jbei.ice.services.rest;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.bulkupload.*;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.entry.PartDefaults;
import org.jbei.ice.lib.entry.sequence.PartSequence;
import org.jbei.ice.lib.utils.Utils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Rest API for interacting with bulk upload resources
 *
 * @author Hector Plahar
 */
@Path("/uploads")
public class BulkUploadResource extends RestResource {

    private BulkUploads controller = new BulkUploads();

    /**
     * Retrieves specified bulk upload resource including
     *
     * @param id
     * @param offset
     * @param limit
     * @return model object for bulk upload
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public BulkUploadInfo read(@PathParam("id") long id,
                               @DefaultValue("0") @QueryParam("offset") int offset,
                               @DefaultValue("50") @QueryParam("limit") int limit) {
        String userId = requireUserId();
        Logger.info(userId + ": retrieving bulk import with id \"" + id + "\"");
        return controller.get(userId, id, offset, limit);
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
    public Response getUploadPermissions(@PathParam("id") long id) {
        String userId = requireUserId();
        List<AccessPermission> permissionList = controller.getUploadPermissions(userId, id);
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
    public Response addPermission(@PathParam("id") long id,
                                  AccessPermission accessPermission) {
        String userId = requireUserId();
        AccessPermission permission = controller.addPermission(userId, id, accessPermission);
        return super.respond(permission);
    }

    /**
     * @param id
     * @param permissionId
     * @return Response with success or failure of permissions delete
     */
    @DELETE
    @Path("/{id}/permissions/{pid}")
    public Response removePermission(@PathParam("id") long id,
                                     @PathParam("pid") long permissionId) {
        String userId = requireUserId();
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
                                       @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        try {
            String fileName = contentDispositionHeader.getFileName();
            String userId = requireUserId();
            String sequence = Utils.getString(fileInputStream);
            SequenceInfo sequenceInfo = controller.addSequence(userId, uploadId, entryId,
                    sequence, fileName);
            if (sequenceInfo == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.status(Response.Status.OK).entity(sequenceInfo).build();
        } catch (IOException e) {
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
                                         @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        String fileName = contentDispositionHeader.getFileName();
        String userId = requireUserId();
        AttachmentInfo attachmentInfo = controller.addAttachment(userId, uploadId,
                entryId, fileInputStream, fileName);
        if (attachmentInfo == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(attachmentInfo).build();
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
    public BulkUploadInfo updateList(@PathParam("id") long id, BulkUploadInfo info) {
        String userId = requireUserId();
        BulkUploadEntries entries = new BulkUploadEntries(userId, id);
        return entries.createOrUpdateEntries(info.getEntryList());
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/link/{linkType}")
    public Response setLink(@PathParam("id") long id, @PathParam("linkType") String linkType) {
        String userId = requireUserId();
        final EntryType entryType = EntryType.nameToType(linkType);
        if (entryType == null)
            throw new WebApplicationException();

        controller.updateLinkType(userId, id, entryType);

        PartDefaults partDefaults = new PartDefaults(userId);
        PartData partData = partDefaults.get(entryType);
        partData.setCustomEntryFields(new CustomFields().get(entryType).getData());
        return super.respond(partData);
    }

    /**
     * @return Response with pending upload information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pending")
    public Response getPendingUploads() {
        String userId = requireUserId();
        HashMap<String, ArrayList<BulkUploadInfo>> pending = controller.getPendingImports(userId);
        return Response.status(Response.Status.OK).entity(pending).build();
    }

    /**
     * @param id
     * @param info
     * @return Response with upload information on renamed upload
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/name")
    public Response updateName(@PathParam("id") long id, BulkUploadInfo info) {
        String userId = getUserId();
        Logger.info(userId + ": updating bulk upload name for " + info.getId() + " with value " + info.getName());
        BulkUploads uploads = new BulkUploads();
        BulkUploadInfo result = uploads.rename(userId, id, info.getName());
        if (result == null) {
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return respond(Response.Status.OK, result);
    }

    /**
     * Updates the status of the bulk upload
     *
     * @param id   unique identifier of bulk upload whose status is to be updated
     * @param info
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") long id, BulkUploadInfo info) {
        String userId = requireUserId();
        Logger.info(userId + ": updating bulk upload status for \"" + info.getId() + "\" to " + info.getStatus());
        BulkUploadEntries entries = new BulkUploadEntries(userId, id);
        ProcessedBulkUpload resp = entries.updateStatus(info.getStatus());
        if (resp.isSuccess())
            return super.respond(resp);
        return super.respond(Response.Status.BAD_REQUEST, resp);
    }

    /**
     * @param uploadId
     * @param data
     * @return Response with created part data
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry")
    public Response createEntry(@PathParam("id") long uploadId, PartData data) {
        String userId = getUserId();
        Logger.info(userId + ": adding entry to upload \"" + uploadId + "\"");
        BulkUploadEntries entries = new BulkUploadEntries(userId, uploadId);
        PartData result = entries.createEntry(data);
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
    public Response updateEntry(@PathParam("id") long uploadId,
                                @PathParam("entryId") long entryId, PartData data) {
        String userId = getUserId();
        Logger.info(userId + ": updating entry \"" + entryId + "\" for upload \"" + uploadId + "\"");
        BulkUploadEntries entries = new BulkUploadEntries(userId, uploadId);
        PartData result = entries.updateEntry(entryId, data);
        return respond(result);
    }

    /**
     * @param info
     * @return a bulk upload info object
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public BulkUploadInfo create(BulkUploadInfo info) {
        String userId = requireUserId();
        Logger.info(userId + ": creating bulk upload draft");
        return controller.create(userId, info);
    }

    /**
     * @return all bulk uploads for the current user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<BulkUploadInfo> query() {
        String userId = getUserId();
        Logger.info(userId + ": retrieving bulk upload drafts");
        return controller.retrieveByUser(userId, userId);
    }

    /**
     * @return Response with the id of the imported bulk upload
     */
    @POST
    @Path("{id}/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@PathParam("id") long uploadId,
                         @FormDataParam("file") InputStream inputStream,
                         @FormDataParam("type") String type,
                         @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        String userId = getUserId();
        EntryType addType = EntryType.valueOf(type.toUpperCase());

        // determine upload format
        String fileName = contentDispositionHeader.getFileName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        FileUploadFormat format = FileUploadFormat.fromString(extension);

        try (FileBulkUpload upload = new FileBulkUpload(userId, inputStream, uploadId, addType, format)) { // todo
            ProcessedBulkUpload processedBulkUpload = upload.process();
            if (processedBulkUpload.isSuccess())
                return Response.status(Response.Status.OK).entity(processedBulkUpload).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(processedBulkUpload).build();
        } catch (Exception e) {
            Logger.error(e);
            ProcessedBulkUpload processedBulkUpload = new ProcessedBulkUpload();
            processedBulkUpload.setUserMessage(e.getMessage());
            processedBulkUpload.setSuccess(false);
            return Response.status(Response.Status.BAD_REQUEST).entity(processedBulkUpload).build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}")
    public Response deleteEntry(@PathParam("id") long uploadId,
                                @PathParam("entryId") long entryId) {
        String userId = getUserId();
        if (controller.deleteEntry(userId, uploadId, entryId)) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }

    /**
     * @param uploadId
     * @param entryId
     * @return OK response if sequence is deleted
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}/sequence")
    public Response deleteEntrySequence(@PathParam("id") long uploadId,
                                        @PathParam("entryId") String entryId) {
        String userId = getUserId();
        PartSequence partSequence = new PartSequence(userId, entryId);
        partSequence.delete();
        return Response.ok().build();
    }

    /**
     * @param uploadId
     * @param entryId
     * @return OK response if attachment is deleted
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entry/{entryId}/attachment")
    public Response deleteEntryAttachment(@PathParam("id") long uploadId,
                                          @PathParam("entryId") long entryId) {
        String userId = getUserId();
        if (controller.deleteAttachment(userId, uploadId, entryId)) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }
}
