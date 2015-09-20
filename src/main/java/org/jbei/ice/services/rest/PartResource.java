package org.jbei.ice.services.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.History;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.entry.*;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sample.SampleService;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.experiment.ExperimentController;
import org.jbei.ice.lib.experiment.Study;
import org.jbei.ice.lib.net.TransferredParts;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
@Path("/parts")
public class PartResource extends RestResource {

    private EntryController controller = new EntryController();
    private EntryRetriever retriever = new EntryRetriever();
    private PermissionsController permissionsController = new PermissionsController();
    private AttachmentController attachmentController = new AttachmentController();
    private SequenceController sequenceController = new SequenceController();
    private ExperimentController experimentController = new ExperimentController();
    private SampleService sampleService = new SampleService();

    /**
     * @param val
     * @param field
     * @param limit
     * @return list of autocomplete values for a field
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete")
    public ArrayList<String> autoComplete(@QueryParam("val") String val,
                                          @DefaultValue("SELECTION_MARKERS") @QueryParam("field") String field,
                                          @DefaultValue("8") @QueryParam("limit") int limit) {
        AutoCompleteField autoCompleteField = AutoCompleteField.valueOf(field.toUpperCase());
        Set<String> result = retriever.getMatchingAutoCompleteField(autoCompleteField, val, limit);
        return new ArrayList<>(result);
    }

    /**
     * @param token
     * @param limit
     * @return list of autocomplete values for parts
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete/partid")
    public ArrayList<PartData> autoComplete(@QueryParam("token") String token,
                                            @DefaultValue("8") @QueryParam("limit") int limit) {
        return retriever.getMatchingPartNumber(token, limit);
    }

    /**
     * Retrieves a part using any of the unique identifiers. e.g. Part number, synthetic id, or
     * global unique identifier
     *
     * @param info
     * @param id
     * @return Response with part data
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response read(@Context final UriInfo info, @PathParam("id") final String id) {
        final String userId = getUserId();
        try {
            log(userId, "retrieving details for " + id);
            final EntryType type = EntryType.nameToType(id);
            PartData data;
            if (type != null) {
                data = controller.getPartDefaults(userId, type);
            } else {
                data = controller.retrieveEntryDetails(userId, id);
            }
            return super.respond(data);
        } catch (final PermissionException pe) {
            // todo : have a generic error entity returned
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    /**
     * @param id
     * @return part data with tooltip information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tooltip")
    public PartData getTooltipDetails(@PathParam("id") final String id) {
        final String userId = getUserId();
        return controller.retrieveEntryTipDetails(userId, id);
    }

    /**
     * @param info
     * @param id
     * @return permissions on the part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public List<AccessPermission> getPermissions(@Context final UriInfo info,
                                                 @PathParam("id") final String id) {
        final String userId = getUserId();
        return retriever.getEntryPermissions(userId, id);
    }

    /**
     * @param info
     * @param partId
     * @param permissions
     * @return part data with permission information
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public PartData setPermissions(@Context final UriInfo info, @PathParam("id") final long partId,
                                   final ArrayList<AccessPermission> permissions) {
        final String userId = getUserId();
        return permissionsController.setEntryPermissions(userId, partId, permissions);
    }

    /**
     * @param partId
     * @return Response with studies on a part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/experiments")
    public Response getPartExperiments(@PathParam("id") final long partId) {
        final String userId = getUserId();
        final List<Study> studies = experimentController.getPartStudies(userId, partId);
        if (studies == null) {
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return respond(Response.Status.OK, studies);
    }

    /**
     * @param partId
     * @param study
     * @return response with study information
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/experiments")
    public Response getPartExperiments(@PathParam("id") final long partId, final Study study) {
        final String userId = getUserId();
        final Study created = experimentController.createStudy(userId, partId, study);
        return respond(Response.Status.OK, created);
    }

    /**
     * @param info
     * @param partId
     * @return Response for success or failure
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response enablePublicAccess(@Context final UriInfo info,
                                       @PathParam("id") final long partId) {
        final String userId = getUserId();
        if (permissionsController.enablePublicReadAccess(userId, partId)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param info
     * @param partId
     * @return Response for success or failure
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response disablePublicAccess(@Context final UriInfo info,
                                        @PathParam("id") final long partId) {
        final String userId = getUserId();
        if (permissionsController.disablePublicReadAccess(userId, partId)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param info
     * @param partId
     * @param permission
     * @return the created permission
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public AccessPermission createPermission(@Context final UriInfo info,
                                             @PathParam("id") final long partId,
                                             final AccessPermission permission) {
        final String userId = getUserId();
        return permissionsController.createPermission(userId, partId, permission);
    }

    /**
     * @param info
     * @param partId
     * @param permissionId
     * @return Response for success or failure
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/{permissionId}")
    public Response removePermission(@Context final UriInfo info,
                                     @PathParam("id") final long partId,
                                     @PathParam("permissionId") final long permissionId) {
        final String userId = getUserId();
        permissionsController.removeEntryPermission(userId, partId, permissionId);
        return super.respond(true);
    }

    /**
     * @param partId
     * @return statistics on part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/statistics")
    public PartStatistics getStatistics(@PathParam("id") final long partId) {
        final String userId = getUserId();
        return controller.retrieveEntryStatistics(userId, partId);
    }

    /**
     * @param info
     * @param partId
     * @return comments on part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments")
    public List<UserComment> getComments(@Context final UriInfo info,
                                         @PathParam("id") final long partId) {
        final String userId = getUserId();
        return controller.retrieveEntryComments(userId, partId);
    }

    /**
     * @param partId
     * @param userComment
     * @return the created comment
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments")
    public Response createComment(@PathParam("id") final long partId,
                                  final UserComment userComment) {
        // todo : check for null
        final String userId = getUserId();
        log(userId, "adding comment to entry " + partId);
        final UserComment comment = controller.createEntryComment(userId, partId, userComment);
        return respond(comment);
    }

    /**
     * @param partId
     * @param commentId
     * @param userComment
     * @return the updated comment
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments/{commentId}")
    public UserComment updateComment(@PathParam("id") final long partId,
                                     @PathParam("commentId") final long commentId,
                                     final UserComment userComment) {
        final String userId = getUserId();
        return controller.updateEntryComment(userId, partId, commentId, userComment);
    }

    /**
     * @param partId
     * @param attachment
     * @return created attachment info
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/attachments")
    public AttachmentInfo addAttachment(@PathParam("id") final long partId,
                                        final AttachmentInfo attachment) {
        // todo : check for null
        final String userId = getUserId();
        final AttachmentController attachmentController = new AttachmentController();
        return attachmentController.addAttachmentToEntry(userId, partId, attachment);
    }

    /**
     * @param partId
     * @return all attachments on a part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/attachments")
    public List<AttachmentInfo> getAttachments(@PathParam("id") final long partId) {
        final String userId = getUserId();
        return attachmentController.getByEntry(userId, partId);
    }

    /**
     * @param info
     * @param partId
     * @param attachmentId
     * @return A response for success or failure
     */
    @DELETE
    @Path("/{id}/attachments/{attachmentId}")
    public Response deleteAttachment(@Context final UriInfo info,
                                     @PathParam("id") final long partId,
                                     @PathParam("attachmentId") final long attachmentId) {
        final String userId = getUserId();
        if (!attachmentController.delete(userId, partId, attachmentId)) {
            return Response.notModified().build();    // todo : use 404 ?
        }
        return Response.ok().build();
    }

    /**
     * @return history entries for the part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/history")
    public ArrayList<History> getHistory(@Context final UriInfo info,
                                         @PathParam("id") final long partId,
                                         @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
                                         @QueryParam("sid") final String sid) {
        String sessionId = StringUtils.isEmpty(userAgentHeader) ? sid : userAgentHeader;
        final String userId = getUserId(sessionId);
        return controller.getHistory(userId, partId);
    }

    /**
     * @param partId
     * @param historyId
     * @return Response for success or failure
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/history/{historyId}")
    public Response delete(@PathParam("id") final long partId,
                           @PathParam("historyId") final long historyId) {
        final String userId = getUserId();
        final boolean success = controller.deleteHistory(userId, partId, historyId);
        return super.respond(success);
    }

    /**
     * @param info
     * @param partId
     * @param sessionId
     * @return traces for the part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/traces")
    public ArrayList<TraceSequenceAnalysis> getTraces(@Context final UriInfo info,
                                                      @PathParam("id") final long partId,
                                                      @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
                                                      @QueryParam("sid") final String sid) {
        String sessionId = StringUtils.isEmpty(userAgentHeader) ? sid : userAgentHeader;
        final String userId = getUserId(sessionId);
        return controller.getTraceSequences(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/traces")
    public Response addTraceSequence(@PathParam("id") final long partId,
                                     @FormDataParam("file") final InputStream fileInputStream,
                                     @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader,
                                     @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
                                     @QueryParam("sid") final String sid) {
        String sessionId = StringUtils.isEmpty(userAgentHeader) ? sid : userAgentHeader;
        final String userId = getUserId(sessionId);
        final String fileName = contentDispositionHeader.getFileName();
        final String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        final File file = Paths.get(tmpDir, fileName).toFile();
        try {
            FileUtils.copyInputStreamToFile(fileInputStream, file);
        } catch (final IOException e) {
            Logger.error(e);
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
        final boolean success = controller.addTraceSequence(userId, partId, file, fileName);
        return respond(success);
    }

    @DELETE
    @Path("/{id}/traces/{traceId}")
    public Response deleteTrace(@Context final UriInfo info,
                                @PathParam("id") final long partId,
                                @PathParam("traceId") final long traceId) {
        final String userId = getUserId();
        if (!controller.deleteTraceSequence(userId, partId, traceId)) {
            return super.respond(Response.Status.UNAUTHORIZED);
        }
        return super.respond(Response.Status.OK);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public ArrayList<PartSample> getSamples(@Context UriInfo info,
                                            @PathParam("id") long partId,
                                            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserId(userAgentHeader);
        return sampleService.retrieveEntrySamples(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public List<PartSample> addSample(@PathParam("id") final long partId,
                                      @QueryParam("strainNamePrefix") final String strainNamePrefix,
                                      final PartSample partSample) {
        final String userId = getUserId();
        log(userId, "creating sample for part " + partId);
        sampleService.createSample(userId, partId, partSample, strainNamePrefix);
        return sampleService.retrieveEntrySamples(userId, partId);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples/{sampleId}")
    public Response deleteSample(@Context UriInfo info, @PathParam("id") long partId,
                                 @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
                                 @PathParam("sampleId") long sampleId) {
        String userId = getUserId(userAgentHeader);
        boolean success = sampleService.delete(userId, partId, sampleId);
        return super.respond(success);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/sequence")
    public Response getSequence(@PathParam("id") final long partId,
                                @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
                                @QueryParam("sid") final String sessionId) {
        final String userId = getUserId(sessionId);
        final FeaturedDNASequence sequence = sequenceController.retrievePartSequence(userId, partId);
        if (sequence == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.OK).entity(sequence).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/sequence")
    public FeaturedDNASequence updateSequence(@PathParam("id") final long partId,
                                              @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
                                              @QueryParam("sid") final String sessionId,
                                              FeaturedDNASequence sequence) {
        final String userId = getUserId(sessionId);
        return sequenceController.updateSequence(userId, partId, sequence);
    }

    @DELETE
    @Path("/{id}/sequence")
    public Response deleteSequence(@PathParam("id") final long partId,
                                   @HeaderParam(value = "X-ICE-Authentication-SessionId") String sid,
                                   @QueryParam("sid") final String sessionId) {
        if (StringUtils.isEmpty(sid))
            sid = sessionId;
        final String userId = getUserId(sid);
        if (sequenceController.deleteSequence(userId, partId)) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartData create(@Context UriInfo info, PartData partData) {
        final String userId = getUserId();
        final EntryCreator creator = new EntryCreator();
        final long id = creator.createPart(userId, partData);
        log(userId, "created entry " + id);
        partData.setId(id);
        return partData;
    }

    /**
     * @param partData
     * @return created part data
     */
    @PUT
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response transfer(PartData partData) {
        TransferredParts transferredParts = new TransferredParts();
        PartData response = transferredParts.receiveTransferredEntry(partData);
        return super.respond(response);
    }

    /**
     * @param info
     * @param partId
     * @param partData
     * @return updated part data
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartData update(@Context final UriInfo info, @PathParam("id") final long partId,
                           final PartData partData) {
        final String userId = getUserId();
        final long id = controller.updatePart(userId, partId, partData);
        log(userId, "updated entry " + id);
        partData.setId(id);
        return partData;
    }

    /**
     * @param id
     */
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") final long id) {
        Logger.info("Deleting part " + id);
        // TODO this does nothing but log?
    }

    /**
     * @param list
     * @return Response for success or failure
     */
    @POST
    @Path("/trash")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response moveToTrash(final ArrayList<PartData> list) {
        final String userId = getUserId();
        final Type fooType = new TypeToken<ArrayList<PartData>>() {
        }.getType();
        final Gson gson = new GsonBuilder().create();
        final ArrayList<PartData> data = gson.fromJson(gson.toJsonTree(list), fooType);
        final boolean success = controller.moveEntriesToTrash(userId, data);
        return respond(success);
    }

    /**
     * Removes the linkId from id
     *
     * @param partId     id of entry whose link we are removing
     * @param linkedPart
     * @return Response for success or failure
     */
    @DELETE
    @Path("/{id}/links/{linkedId}")
    public Response deleteLink(@PathParam("id") final long partId,
                               @DefaultValue("CHILD") @QueryParam("linkType") LinkType linkType,
                               @PathParam("linkedId") final long linkedPart) {
        final String userId = getUserId();
        log(userId, "removing link " + linkedPart + " from " + partId);
        EntryLinks entryLinks = new EntryLinks(userId, partId);
        final boolean success = entryLinks.removeLink(linkedPart, linkType);
        return respond(success);
    }

    /**
     * Creates a new link between the referenced part id and the part in the parameter
     *
     * @param partId    part to be linked
     * @param partData  should essentially just contain the part Id or details for a new entry that should be created
     * @param sessionId unique session identifier for user performing action
     * @return todo
     */
    @POST
    @Path("/{id}/links")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLink(@PathParam("id") long partId,
                               @QueryParam("linkType") @DefaultValue("CHILD") LinkType type,
                               @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId,
                               PartData partData) {
        String userId = getUserId(sessionId);
        log(userId, "adding entry link " + partData.getId() + " to " + partId);
        EntryLinks entryLinks = new EntryLinks(userId, partId);
        return super.respond(entryLinks.addLink(partData, type));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEntries(@QueryParam(value = "visibility") Visibility visibility,
                                  List<Long> entryIds) {
        String userId = getUserId();
        log(userId, "updating visibility of " + entryIds.size() + " entries to " + visibility);
        Entries entries = new Entries();
        List<Long> arrayList = new ArrayList<>();
        for (Number id : entryIds)
            arrayList.add(id.longValue());
        boolean success = entries.updateVisibility(userId, arrayList, visibility);
        return super.respond(success);
    }
}
