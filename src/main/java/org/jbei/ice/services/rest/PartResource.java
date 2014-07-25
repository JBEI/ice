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
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.dto.sample.SampleStorage;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryRetriever;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
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
@Path("/part")
public class PartResource extends RestResource {

    private EntryController controller = new EntryController();
    private EntryRetriever retriever = new EntryRetriever();
    private PermissionsController permissionsController = new PermissionsController();
    private AttachmentController attachmentController = new AttachmentController();
    private SequenceController sequenceController = new SequenceController();

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
     * @param userAgentHeader
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public PartData read(@Context UriInfo info,
            @PathParam("id") String id,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.retrieveEntryDetails(userId, id);
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
        String userId = getUserIdFromSessionHeader(userAgentHeader);
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
    public AccessPermission createComment(@Context UriInfo info, @PathParam("id") long partId,
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
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.retrieveEntryStatistics(userId, partId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments")
    public ArrayList<UserComment> getComments(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.retrieveEntryComments(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments")
    public UserComment createComment(@PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            UserComment userComment) {
//        if(userComment == null || userComment.getMessage() == null)
//            throw new Web
        // todo : check for null
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.createEntryComment(userId, partId, userComment);
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
        String userId = getUserIdFromSessionHeader(userAgentHeader);
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
    @Path("/{id}/traces")
    public ArrayList<TraceSequenceAnalysis> getTraces(@Context UriInfo info,
            @PathParam("id") long partId,
            @QueryParam("sid") String sessionId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        if (StringUtils.isEmpty(userAgentHeader))
            userAgentHeader = sessionId;
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.getTraceSequences(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/traces")
    public void addTraceSequence(@PathParam("id") long partId,
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
            return;
        }
        controller.addTraceSequence(userId, partId, file, fileName);
    }

    @DELETE
    @Path("/{id}/traces/{traceId}")
    public void deleteTrace(@Context UriInfo info, @PathParam("id") long partId,
            @PathParam("traceId") long traceId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        controller.deleteTraceSequence(userId, partId, traceId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public ArrayList<SampleStorage> getSamples(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.retrieveEntrySamples(userId, partId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/sequence")
    public FeaturedDNASequence getSequence(@PathParam("id") long partId,
            @QueryParam("sid") String sessionId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        if (StringUtils.isEmpty(userAgentHeader))
            userAgentHeader = sessionId;

        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return new SequenceController().retrievePartSequence(userId, partId);
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
        partData.setId(id);
        return partData;
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
        partData.setId(id);
        return partData;
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        Logger.info("Deleting part " + id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response moveToTrash(ArrayList<PartData> list,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Type fooType = new TypeToken<ArrayList<PartData>>() {
        }.getType();
        Gson gson = new GsonBuilder().create();
        ArrayList<PartData> data = gson.fromJson(gson.toJsonTree(list), fooType);
        if (controller.moveEntriesToTrash(userId, data))
            return respond(Response.Status.OK);
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
