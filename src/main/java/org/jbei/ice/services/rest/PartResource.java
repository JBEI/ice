package org.jbei.ice.services.rest;

import java.util.ArrayList;
import java.util.Set;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
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
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

import org.apache.commons.lang.StringUtils;

/**
 * @author Hector Plahar
 */
@Path("/part")
public class PartResource extends RestResource {

    private EntryController controller = new EntryController();
    private EntryRetriever retriever = new EntryRetriever();
    private PermissionsController permissionsController = new PermissionsController();
    private AttachmentController attachmentController = new AttachmentController();

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public PartData read(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        return controller.retrieveEntryDetails(userId, partId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public ArrayList<AccessPermission> getPermissions(@Context UriInfo info, @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        Entry entry = DAOFactory.getEntryDAO().get(partId);
        return new PermissionsController().retrieveSetEntryPermissions(entry);
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

    @DELETE
    @Path("/{id}/traces/{traceId}")
    public void deleteTrace(@Context UriInfo info, @PathParam("id") long partId, @PathParam("traceId") long traceId,
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
        return new SequenceController().updateSequence(userId, partId, sequence);
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
        PartData data = new PartData();
        data.setId(id);
        return data;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartData update(@Context UriInfo info,
            @PathParam("id") long partId,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader,
            PartData partData) {
        // tODO : update not create;
        String userId = getUserIdFromSessionHeader(userAgentHeader);
        EntryCreator creator = new EntryCreator();
        long id = creator.createPart(userId, partData);
        PartData data = new PartData();
        data.setId(id);
        return data;
    }

//    @Path("/{id}")
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response update(@PathParam("id") long id, PartTransfer partTransfer) {
//        return Response.ok().build();
//    }

    // can also have a create method that returns Response object with the location of the created object
    // Response.created(uri).entity(resource).build()

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") long id) {
        Logger.info("Deleting part " + id);
    }

    @DELETE
    public Response deleteEntries(ArrayList<PartData> list,
            @HeaderParam(value = "X-ICE-Authentication-SessionId") String userAgentHeader) {
        return respond(Response.Status.OK);
    }
}
