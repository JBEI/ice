package org.jbei.ice.services.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.History;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryRetriever;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.experiment.ExperimentController;
import org.jbei.ice.lib.experiment.Study;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete/partid")
    public ArrayList<PartData> autoComplete(@QueryParam("token") String token,
            @DefaultValue("8") @QueryParam("limit") int limit) {
        return retriever.getMatchingPartNumber(token, limit);
    }

    /**
     * Retrieves a part using any of the unique identifiers. e.g. Part number, synthetic id, or global unique
     * identifier
     *
     * @param info
     * @param id
     * @param sessionId unique session identifier
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response read(@Context UriInfo info,
            @PathParam("id") String id,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = SessionHandler.getUserIdBySession(sessionId);
        log(userId, "retrieving details for " + id);
        EntryType type = EntryType.nameToType(id);
        PartData data;
        if (type != null)
            data = controller.getPartDefaults(userId, type);
        else
            data = controller.retrieveEntryDetails(userId, id);
        return super.respond(data);
    }

    /**
     * Retrieves a comma separated value representation of the part referenced by the path parameter identifier
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/csv")
    public Response getCSV(
            @PathParam("id") String id,
            @QueryParam("sid") String sid,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        if (StringUtils.isEmpty(sessionId))
            sessionId = sid;

        String userId = getUserIdFromSessionHeader(sessionId);
        log(userId, "retrieving part csv");
        String csv = retriever.getAsCSV(userId, id);
        if (csv != null) {
            String name = retriever.getPartNumber(userId, id);
            if (name == null)
                name = "entry.csv";
            else
                name += ".csv";
            Response.ResponseBuilder response = Response.ok(csv);
            response.header("Content-Disposition", "attachment; filename=\"" + name + "\"");
            return response.build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tooltip")
    public PartData getTooltipDetails(@PathParam("id") String id,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = SessionHandler.getUserIdBySession(userAgentHeader);
        return controller.retrieveEntryTipDetails(userId, id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public ArrayList<AccessPermission> getPermissions(@Context UriInfo info, @PathParam("id") String id,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return retriever.getEntryPermissions(userId, id);
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/experiments")
    public Response getPartExperiments(@PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        ArrayList<Study> studies = experimentController.getPartStudies(userId, partId);
        if (studies == null)
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        return respond(Response.Status.OK, studies);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/experiments")
    public Response getPartExperiments(@PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            Study study) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        study = experimentController.createStudy(userId, partId, study);
        return respond(Response.Status.OK, study);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response enablePublicAccess(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        if (permissionsController.enablePublicReadAccess(userId, partId))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response disablePublicAccess(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        if (permissionsController.disablePublicReadAccess(userId, partId))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public AccessPermission createPermission(@Context UriInfo info, @PathParam("id") long partId,
            AccessPermission permission,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return permissionsController.createPermission(userId, partId, permission);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/{permissionId}")
    public Response removePermission(@Context UriInfo info,
            @PathParam("id") long partId,
            @PathParam("permissionId") long permissionId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        permissionsController.removeEntryPermission(userId, partId, permissionId);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/statistics")
    public PartStatistics getStatistics(@PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = SessionHandler.getUserIdBySession(userAgentHeader);
        return controller.retrieveEntryStatistics(userId, partId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments")
    public ArrayList<UserComment> getComments(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = SessionHandler.getUserIdBySession(userAgentHeader);
        return controller.retrieveEntryComments(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments")
    public Response createComment(@PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            UserComment userComment) {
//        if(userComment == null || userComment.getMessage() == null)
//            throw new Web
        // todo : check for null
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        log(userId, "adding comment to entry " + partId);
        UserComment comment = controller.createEntryComment(userId, partId, userComment);
        return respond(comment);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments/{commentId}")
    public UserComment updateComment(@PathParam("id") long partId,
            @PathParam("commentId") long commentId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            UserComment userComment) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.updateEntryComment(userId, partId, commentId, userComment);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/attachments")
    public AttachmentInfo addAttachment(@PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            AttachmentInfo attachment) {
        // todo : check for null
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        AttachmentController attachmentController = new AttachmentController();
        return attachmentController.addAttachmentToEntry(userId, partId, attachment);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/attachments")
    public ArrayList<AttachmentInfo> getAttachments(@PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = SessionHandler.getUserIdBySession(userAgentHeader);
        return attachmentController.getByEntry(userId, partId);
    }

    @DELETE
    @Path("/{id}/attachments/{attachmentId}")
    public Response deleteAttachment(@Context UriInfo info,
            @PathParam("id") long partId,
            @PathParam("attachmentId") long attachmentId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        if (!attachmentController.delete(userId, partId, attachmentId))
            return Response.notModified().build();    // todo : use 404 ?
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/history")
    public ArrayList<History> getHistory(@Context UriInfo info,
            @PathParam("id") long partId,
            @QueryParam("sid") String sessionId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        if (StringUtils.isEmpty(userAgentHeader))
            userAgentHeader = sessionId;
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.getHistory(userId, partId);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/history/{historyId}")
    public Response delete(@PathParam("id") long partId,
            @PathParam("historyId") long historyId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        boolean success = controller.deleteHistory(userId, partId, historyId);
        return super.respond(success);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/traces")
    public ArrayList<TraceSequenceAnalysis> getTraces(@Context UriInfo info,
            @PathParam("id") long partId,
            @QueryParam("sid") String sessionId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        if (StringUtils.isEmpty(userAgentHeader))
            userAgentHeader = sessionId;
        String userId = SessionHandler.getUserIdBySession(userAgentHeader);
        return controller.getTraceSequences(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/traces")
    public Response addTraceSequence(@PathParam("id") long partId,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @QueryParam("sid") String sessionId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        if (StringUtils.isEmpty(userAgentHeader))
            userAgentHeader = sessionId;
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        String fileName = contentDispositionHeader.getFileName();
        String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        File file = Paths.get(tmpDir, fileName).toFile();
        try {
            FileUtils.copyInputStreamToFile(fileInputStream, file);
        } catch (IOException e) {
            Logger.error(e);
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }
        boolean success = controller.addTraceSequence(userId, partId, file, fileName);
        return respond(success);
    }

    @DELETE
    @Path("/{id}/traces/{traceId}")
    public Response deleteTrace(@Context UriInfo info, @PathParam("id") long partId,
            @PathParam("traceId") long traceId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        if (!controller.deleteTraceSequence(userId, partId, traceId))
            return super.respond(Response.Status.UNAUTHORIZED);
        return super.respond(Response.Status.OK);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public ArrayList<PartSample> getSamples(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = SessionHandler.getUserIdBySession(userAgentHeader);
        SampleController sampleController = new SampleController();
        return sampleController.retrieveEntrySamples(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public ArrayList<PartSample> addSample(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            PartSample partSample) {
        String userId = SessionHandler.getUserIdBySession(userAgentHeader);
        SampleController sampleController = new SampleController();
        sampleController.createSample(userId, partId, partSample);
        return sampleController.retrieveEntrySamples(userId, partId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/sequence")
    public Response getSequence(@PathParam("id") long partId,
            @QueryParam("sid") String sessionId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        if (StringUtils.isEmpty(userAgentHeader))
            userAgentHeader = sessionId;

        String userId = SessionHandler.getUserIdBySession(userAgentHeader);
        FeaturedDNASequence sequence = new SequenceController().retrievePartSequence(userId, partId);
        if (sequence == null)
            return Response.status(Response.Status.NO_CONTENT).build();
        return Response.status(Response.Status.OK).entity(sequence).build();
    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/{id}/sequence/visual")
//    public String getVisual(@PathParam("id") long partId,
//            @QueryParam("sid") String sessionId,
//            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
//        if (StringUtils.isEmpty(userAgentHeader))
//            userAgentHeader = sessionId;
//
//        String userId = getUserIdFromSessionHeader(userAgentHeader);
//        return new SequenceController().retrievePartSequence(userId, partId);
//    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/sequence")
    public FeaturedDNASequence updateSequence(@PathParam("id") long partId,
            @QueryParam("sid") String sessionId,
            FeaturedDNASequence sequence,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        if (StringUtils.isEmpty(userAgentHeader))
            userAgentHeader = sessionId;

        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return sequenceController.updateSequence(userId, partId, sequence);
    }

    @DELETE
    @Path("/{id}/sequence")
    public Response deleteSequence(@PathParam("id") long partId,
            @QueryParam("sid") String sessionId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        try {
            if (sequenceController.deleteSequence(userId, partId))
                return Response.ok().build();
            return Response.serverError().build();
        } catch (RuntimeException e) {
            Logger.error(e);
            return Response.serverError().build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartData create(@Context UriInfo info,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            PartData partData) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        EntryCreator creator = new EntryCreator();
        long id = creator.createPart(userId, partData);
        log(userId, "created entry " + id);
        partData.setId(id);
        return partData;
    }

    @PUT
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response transfer(PartData partData) {
        EntryCreator creator = new EntryCreator();
        PartData response = creator.receiveTransferredEntry(partData);
        return super.respond(Response.Status.OK, response);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartData update(@Context UriInfo info,
            @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            PartData partData) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        long id = controller.updatePart(userId, partId, partData);
        log(userId, "updated entry " + id);
        partData.setId(id);
        return partData;
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        Logger.info("Deleting part " + id);
    }

    @POST
    @Path("/trash")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response moveToTrash(ArrayList<PartData> list,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Type fooType = new TypeToken<ArrayList<PartData>>() {
        }.getType();
        Gson gson = new GsonBuilder().create();
        ArrayList<PartData> data = gson.fromJson(gson.toJsonTree(list), fooType);
        boolean success = controller.moveEntriesToTrash(userId, data);
        return respond(success);
    }

    /**
     * Removes the linkId from id
     *
     * @param partId     id of entry whose link we are removing
     * @param linkedPart
     * @param sessionId
     * @return
     */
    @DELETE
    @Path("/{id}/links/{linkedId}")
    public Response deleteLink(@PathParam("id") long partId,
            @PathParam("linkedId") long linkedPart,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String sessionId) {
        String userId = getUserIdFromSessionHeader(sessionId);
        log(userId, "removing link " + linkedPart + " from " + partId);
        boolean success = controller.removeLink(userId, partId, linkedPart);
        return respond(success);
    }
}
