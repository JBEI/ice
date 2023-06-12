package org.jbei.ice.services.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.bulkupload.*;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.entry.PartDefaults;
import org.jbei.ice.entry.sequence.PartSequence;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Rest API for interacting with bulk upload resources
 *
 * @author Hector Plahar
 */
@Path("/uploads")
public class BulkUploadResource extends RestResource {

    private final BulkUploads bulkUploads = new BulkUploads();

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
    public BulkUpload read(@PathParam("id") long id,
                           @DefaultValue("0") @QueryParam("offset") int offset,
                           @DefaultValue("50") @QueryParam("limit") int limit) {
        String userId = requireUserId();
        Logger.info(userId + ": retrieving bulk import with id \"" + id + "\"");
        return bulkUploads.get(userId, id, offset, limit);
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
        ArrayList<String> results = bulkUploads.getMatchingPartNumbersForLinks(uploadType, token, limit);
        return super.respond(results);
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
            SequenceInfo sequenceInfo = bulkUploads.addSequence(userId, uploadId, entryId,
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
        AttachmentInfo attachmentInfo = bulkUploads.addAttachment(userId, uploadId,
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
    public BulkUpload updateList(@PathParam("id") long id, BulkUpload info) {
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

        bulkUploads.updateLinkType(userId, id, entryType);

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
        HashMap<String, ArrayList<BulkUpload>> pending = bulkUploads.getPendingImports(userId);
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
    public Response updateName(@PathParam("id") long id, BulkUpload info) {
        String userId = getUserId();
        Logger.info(userId + ": updating bulk upload name for " + info.getId() + " with value " + info.getName());
        BulkUploads uploads = new BulkUploads();
        BulkUpload result = uploads.rename(userId, id, info.getName());
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
    public Response updateStatus(@PathParam("id") long id, BulkUpload info) {
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
    public BulkUpload create(BulkUpload info) {
        String userId = requireUserId();
        Logger.info(userId + ": creating bulk upload draft");
        return bulkUploads.create(userId, info);
    }

    /**
     * @return all bulk uploads for the current user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<BulkUpload> query() {
        String userId = getUserId();
        Logger.info(userId + ": retrieving bulk upload drafts");
        return bulkUploads.retrieveByUser(userId, userId);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/file")
    public Response createUploadFromFile(@FormDataParam("file") InputStream inputStream,
                                         @FormDataParam("type") String type,
                                         @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        String userId = getUserId();
        EntryType addType = EntryType.valueOf(type.toUpperCase());
        String filename = contentDispositionHeader.getFileName();

        try (FileBulkUpload upload = new FileBulkUpload(userId, inputStream, filename, addType)) {
            return super.respond(upload.uploadFile());
        } catch (IOException e) {
            Logger.error(e);
            ProcessedBulkUpload processedBulkUpload = new ProcessedBulkUpload();
            processedBulkUpload.setUserMessage(e.getMessage());
            processedBulkUpload.setSuccess(false);
            return Response.status(Response.Status.BAD_REQUEST).entity(processedBulkUpload).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/validation")
    public Response validateFileUpload(@PathParam("id") long uploadId) {
        String userId = getUserId();
        BulkUploadValidation validation = new BulkUploadValidation(userId, uploadId);
        validation.perform();
        return super.respond(false);
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
        if (bulkUploads.deleteEntry(userId, uploadId, entryId)) {
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
        if (bulkUploads.deleteAttachment(userId, uploadId, entryId)) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }
}
