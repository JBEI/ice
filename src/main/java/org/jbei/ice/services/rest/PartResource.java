package org.jbei.ice.services.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.ShotgunSequenceDTO;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.*;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sample.SampleService;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.TraceSequences;
import org.jbei.ice.lib.entry.sequence.annotation.Annotations;
import org.jbei.ice.lib.experiment.Experiments;
import org.jbei.ice.lib.experiment.Study;
import org.jbei.ice.lib.net.RemoteEntries;
import org.jbei.ice.lib.net.TransferredParts;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.ShotgunSequenceDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.ShotgunSequence;

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
import java.util.Date;
import java.util.List;

/**
 * Rest resource for interacting representations of biological parts on ICE
 *
 * @author Hector Plahar
 */
@Path("/parts")
public class PartResource extends RestResource {

    private EntryController controller = new EntryController();
    private AttachmentController attachmentController = new AttachmentController();
    private SequenceController sequenceController = new SequenceController();
    private SampleService sampleService = new SampleService();
    private RemoteEntries remoteEntries = new RemoteEntries();

    /**
     * Retrieves a part using any of the unique identifiers. e.g. Part number, synthetic id, or
     * global unique identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response read(@PathParam("id") final String id,
                         @DefaultValue("false") @QueryParam("remote") boolean isRemote,
                         @QueryParam("token") String remoteUserToken,      // todo : move to header
                         @QueryParam("userId") String remoteUserId,
                         @QueryParam("folderId") long fid) {
        String userId = getUserId();
        if (isRemote) {
            log(userId, "get remote entry");
            long partId = Long.decode(id);
            PartData data = remoteEntries.getEntryDetails(userId, fid, partId);
            return super.respond(data);
        } else {
            try {
                if (StringUtils.isEmpty(userId)) {
                    RegistryPartner partner = requireWebPartner();
                    log(partner.getUrl(), "retrieving details for " + id);
                    return super.respond(controller.getRequestedEntry(remoteUserId, remoteUserToken, id, fid, partner));
                } else {
                    log(userId, "retrieving details for " + id);
                    return super.respond(controller.retrieveEntryDetails(userId, id));
                }
            } catch (final PermissionException pe) {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }
        }
    }

    /**
     * Retrieves the defaults for the requested entry types.
     *
     * @param type the type of entry i.e. <code>PLASMID</code>, <code>PART</code>, <code>STRAIN</code> or
     *             <code>ARABIDOPSIS</code>. Used to retrieve the default values for that entry
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/defaults/{type}")
    public Response get(@PathParam("type") String type) {
        String userId = requireUserId();
        final EntryType entryType = EntryType.nameToType(type);
        if (entryType == null)
            throw new WebApplicationException();

        PartDefaults partDefaults = new PartDefaults(userId);
        return super.respond(partDefaults.get(entryType));
    }

    /**
     * Returns the folders that an entry is contained in (filtered by permissions).
     *
     * @param id unique identifier for entry whose containing folders are being returned
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/folders")
    public Response getEntryFolders(@PathParam("id") String id) {
        // user id is allowed to be empty. The entry has to be public in that instance
        // and only public folders it is contained in is returned
        String userId = getUserId();
        EntryFolders entryFolders = new EntryFolders(userId, id);
        return super.respond(entryFolders.getFolders());
    }

    /**
     * Retrieves either a remote or local tooltip
     *
     * @param id       unique identifier for entry whose tooltip is to be retrieved
     * @param isRemote if true, a remote tooltip is being retrieved, false, local
     * @param fid      required if <code>isRemote</code>  is true. The remote entry should be shared in this folder
     * @return PartData information about the entry with enough information to show the tooltip
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tooltip")
    public PartData getTooltipDetails(@PathParam("id") final String id,
                                      @DefaultValue("false") @QueryParam("remote") boolean isRemote,
                                      @QueryParam("folderId") long fid) {
        final String userId = getUserId();
        if (isRemote) {
            log(userId, " get remote tooltip");
            long partId = Long.decode(id);
            return remoteEntries.retrieveRemoteToolTip(userId, fid, partId);
        }

        if (StringUtils.isEmpty(userId)) {
            requireWebPartner();
        }
        return controller.retrieveEntryTipDetails(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public Response getPermissions(@PathParam("id") final String id) {
        final String userId = requireUserId();
        try {
            EntryPermissions entryPermissions = new EntryPermissions(id, userId);
            List<AccessPermission> permissions = entryPermissions.getEntryPermissions();
            return super.respond(permissions);
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions")
    public AccessPermission createPermission(@PathParam("id") final String partId,
                                             final AccessPermission permission) {
        final String userId = requireUserId();
        try {
            EntryPermissions permissions = new EntryPermissions(partId, userId);
            return permissions.add(permission);
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/{permissionId}")
    public Response removePermission(@PathParam("id") final String partId,
                                     @PathParam("permissionId") final long permissionId) {
        final String userId = requireUserId();
        log(userId, "removing permission " + permissionId + " from entry " + partId);
        try {
            EntryPermissions entryPermissions = new EntryPermissions(partId, userId);
            entryPermissions.removePermission(permissionId);
            return Response.ok().build();
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/experiments")
    public Response getPartExperiments(@PathParam("id") final String partId) {
        final String userId = requireUserId();
        try {
            Experiments experiments = new Experiments(userId, partId);
            final List<Study> studies = experiments.getPartStudies();
            return respond(Response.Status.OK, studies);
        } catch (PermissionException e) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        } catch (IllegalArgumentException ile) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/experiments")
    public Response createPartExperiment(@PathParam("id") final String partId,
                                         final Study study) {
        try {
            final String userId = requireUserId();
            log(userId, "adding experiment " + study.toString());
            Experiments experiments = new Experiments(userId, partId);
            final Study created = experiments.createOrUpdateStudy(study);
            return respond(Response.Status.OK, created);
        } catch (PermissionException e) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        } catch (IllegalArgumentException ile) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @DELETE
    @Path("/{id}/experiments/{eid}")
    public Response deletePartExperiment(
            @PathParam("id") final String partId,
            @PathParam("eid") final long experimentId) {
        try {
            String userId = requireUserId();
            log(userId, "deleting experiment " + experimentId + " for entry " + partId);
            Experiments experiments = new Experiments(userId, partId);
            return super.respond(experiments.deleteStudy(experimentId));
        } catch (PermissionException e) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        } catch (IllegalArgumentException ile) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response enablePublicAccess(@PathParam("id") final String partId) {
        final String userId = requireUserId();
        log(userId, "adding public read access for part " + partId);
        try {
            EntryPermissions entryPermissions = new EntryPermissions(partId, userId);
            entryPermissions.enablePublicReadAccess();
            return Response.ok().build();
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/permissions/public")
    public Response disablePublicAccess(@PathParam("id") final String partId) {
        final String userId = requireUserId();
        log(userId, "removing public read access for part " + partId);
        try {
            EntryPermissions entryPermissions = new EntryPermissions(partId, userId);
            entryPermissions.disablePublicReadAccess();
            return Response.ok().build();
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/statistics")
    public PartStatistics getStatistics(@PathParam("id") final long partId) {
        final String userId = getUserId();
        try {
            return controller.retrieveEntryStatistics(userId, partId);
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments")
    public List<UserComment> getComments(@PathParam("id") final long partId) {
        final String userId = getUserId();
        return controller.retrieveEntryComments(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments")
    public Response createComment(@PathParam("id") final long partId,
                                  final UserComment userComment) {
        final String userId = getUserId();
        log(userId, "adding comment to entry " + partId);
        final UserComment comment = controller.createEntryComment(userId, partId, userComment);
        return respond(comment);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/comments/{commentId}")
    public UserComment updateComment(@PathParam("id") final long partId,
                                     @PathParam("commentId") final long commentId,
                                     final UserComment userComment) {
        final String userId = getUserId();
        return controller.updateEntryComment(userId, partId, commentId, userComment);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/attachments")
    public AttachmentInfo addAttachment(@PathParam("id") final long partId,
                                        final AttachmentInfo attachment) {
        final String userId = getUserId();
        final AttachmentController attachmentController = new AttachmentController();
        return attachmentController.addAttachmentToEntry(userId, partId, attachment);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/attachments")
    public List<AttachmentInfo> getAttachments(@PathParam("id") final long partId) {
        final String userId = getUserId();
        return attachmentController.getByEntry(userId, partId);
    }

    @DELETE
    @Path("/{id}/attachments/{attachmentId}")
    public Response deleteAttachment(@PathParam("id") final long partId,
                                     @PathParam("attachmentId") final long attachmentId) {
        final String userId = getUserId();
        return super.respond(attachmentController.delete(userId, partId, attachmentId));
    }

    /**
     * @return history entries for the part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/history")
    public Response getHistory(
            @PathParam("id") long partId,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset,
            @QueryParam("asc") boolean asc,
            @QueryParam("sort") String sort) {
        String userId = requireUserId();
        EntryHistory entryHistory = new EntryHistory(userId, partId);
        return super.respond(entryHistory.get(limit, offset, asc, sort));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/history/{historyId}")
    public Response delete(@PathParam("id") final long partId,
                           @PathParam("historyId") final long historyId) {
        final String userId = requireUserId();
        EntryHistory entryHistory = new EntryHistory(userId, partId);
        return super.respond(entryHistory.delete(historyId));
    }

    /**
     * @return traces for the part
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/traces")
    public Response getTraces(
            @Context final UriInfo info,
            @PathParam("id") final long partId,
            @DefaultValue("100") @QueryParam("limit") int limit,
            @DefaultValue("0") @QueryParam("start") int start) {
        final String userId = getUserId();
        TraceSequences traceSequences = new TraceSequences(userId, partId);
        Results<TraceSequenceAnalysis> results = traceSequences.getTraces(start, limit);

        // hack for trace sequence viewer without having to modify it
        if (StringUtils.isEmpty(sessionId))
            return super.respond(new ArrayList<>(results.getData()));
        return super.respond(results);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/shotgunsequences")
    public ArrayList<ShotgunSequenceDTO> getShotgunSequences(
            @PathParam("id") final long partId) {
        final String userId = getUserId();
        ShotgunSequenceDAO dao = DAOFactory.getShotgunSequenceDAO();
        final EntryDAO entryDAO = DAOFactory.getEntryDAO();
        final Entry entry = entryDAO.get(partId);

        if (entry == null) {
            return null;
        }

        ArrayList<ShotgunSequenceDTO> returns = new ArrayList<>();
        List<ShotgunSequence> results = dao.getByEntry(entry, userId);

        for (ShotgunSequence ret : results) {
            returns.add(new ShotgunSequenceDTO(ret));
        }

        Logger.info("Shotgun Sequences requested for entry " + partId);
        return returns;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/traces")
    public Response addTraceSequence(@PathParam("id") final long partId,
                                     @FormDataParam("file") final InputStream fileInputStream,
                                     @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        final String userId = getUserId();
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/shotgunsequences")
    public Response addShotgunSequence(@PathParam("id") final long partId,
                                       @FormDataParam("file") final InputStream fileInputStream,
                                       @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        final String userId = getUserId();
        final String fileName = contentDispositionHeader.getFileName();
        final EntryDAO entryDAO = DAOFactory.getEntryDAO();
        final Entry entry = entryDAO.get(partId);
        ShotgunSequenceDAO dao = DAOFactory.getShotgunSequenceDAO();

        try {
            String storageName = Utils.generateUUID();
            dao.writeSequenceFileToDisk(storageName, fileInputStream);
            dao.create(fileName, userId, entry, storageName, new Date());
        } catch (Exception e) {
            Logger.error(e);
            return respond(Response.Status.INTERNAL_SERVER_ERROR);
        }

        Logger.info("Uploaded shotgun sequence for entry " + entry.getId());
        return respond(Response.Status.OK);
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
    public ArrayList<PartSample> getSamples(@PathParam("id") long partId) {
        String userId = requireUserId();
        return sampleService.retrieveEntrySamples(userId, partId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples")
    public Response addSample(@PathParam("id") final long partId,
                              @QueryParam("strainNamePrefix") final String strainNamePrefix,
                              final PartSample partSample) {
        final String userId = getUserId();
        log(userId, "creating sample for part " + partId);
        sampleService.createSample(userId, partId, partSample, strainNamePrefix);
        ArrayList<PartSample> result = sampleService.retrieveEntrySamples(userId, partId);
        Results<PartSample> results = new Results<>();
        results.setData(result);
        return super.respond(results);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/samples/{sampleId}")
    public Response deleteSample(@Context UriInfo info, @PathParam("id") long partId,
                                 @PathParam("sampleId") long sampleId) {
        String userId = requireUserId();
        log(userId, "deleting sample " + sampleId + " for part " + partId);
        boolean success = sampleService.delete(userId, partId, sampleId);
        return super.respond(success);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/sequence")
    public Response getSequence(@PathParam("id") final String partId,
                                @DefaultValue("false") @QueryParam("remote") boolean isRemote,
                                @QueryParam("token") String remoteUserToken,
                                @QueryParam("userId") String remoteUserId,
                                @QueryParam("folderId") long fid) {
        final FeaturedDNASequence sequence;
        final String userId = getUserId();

        try {
            if (isRemote) {
                // entry exists remotely
                sequence = remoteEntries.getSequence(userId, fid, partId);
            } else {
                // what request is being responded to (local or remote)
                if (StringUtils.isEmpty(userId)) {
                    RegistryPartner partner = requireWebPartner();
                    if (StringUtils.isEmpty(remoteUserToken) || fid == 0) {
                        sequence = sequenceController.retrievePartSequence(userId, partId);
                    } else {
                        sequence = sequenceController.getRequestedSequence(partner, remoteUserId, remoteUserToken, partId, fid);
                    }
                } else {
                    // user id can be null if partId is public
                    sequence = sequenceController.retrievePartSequence(userId, partId);
                }
            }
            return Response.status(Response.Status.OK).entity(sequence).build();
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    // put should be used to update when the new vector editor implementation is in place
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/sequence")
    public Response updateSequence(@PathParam("id") final long partId,
                                   @DefaultValue("false") @QueryParam("add") boolean add,
                                   FeaturedDNASequence sequence) {
        final String userId = requireUserId();
        return super.respond(sequenceController.updateSequence(userId, partId, sequence, add));
    }

    @DELETE
    @Path("/{id}/sequence")
    public Response deleteSequence(@PathParam("id") final long partId) {
        final String userId = requireUserId();
        try {
            return super.respond(sequenceController.deleteSequence(userId, partId));
        } catch (PermissionException e) {
            Logger.error(e);
            throw new WebApplicationException(e.getMessage(), Response.Status.FORBIDDEN);
        }
    }

    /**
     * Creates a new entry. If the <code>sourceId</code> parameter is set, the new entry is a copy
     * of the source id (if found) otherwise the new entry is created from the data contained in the
     * <code>partData</code>
     *
     * @param sourceId optional unique identifier for an existing part to copy. If not set, the <code>partData</code>
     *                 parameter must be set
     * @param partData optional data for creating new entry. if not set, then the <code>sourceId</code> must
     *                 be set
     * @return wrapper around identifier for newly created part which can be used to retrieve it
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@QueryParam("source") String sourceId,
                           PartData partData) {
        final String userId = requireUserId();
        final EntryCreator creator = new EntryCreator();
        if (StringUtils.isEmpty(sourceId)) {
            log(userId, "created new " + partData.getType().getDisplay());
            return super.respond(creator.createPart(userId, partData));
        }

        try {
            log(userId, "creating copy of entry " + sourceId);
            return super.respond(creator.copyPart(userId, sourceId));
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

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
     * Update the part information at the specified resource identifier
     *
     * @param partId   unique resource identifier for part being updated
     * @param partData data to update part with
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") final long partId,
                           final PartData partData) {
        final String userId = requireUserId();
        try {
            final long id = controller.updatePart(userId, partId, partData);
            log(userId, "update entry " + id);
            partData.setId(id);
            return super.respond(partData);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @POST
    @Path("/trash")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response moveToTrash(final ArrayList<PartData> list) {
        try {
            final String userId = getUserId();
            final Type fooType = new TypeToken<ArrayList<PartData>>() {
            }.getType();
            final Gson gson = new GsonBuilder().create();
            final ArrayList<PartData> data = gson.fromJson(gson.toJsonTree(list), fooType);
            final boolean success = controller.moveEntriesToTrash(userId, data);
            return respond(success);
        } catch (PermissionException pe) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    /**
     * Get linked parts
     *
     * @param partId unique identifier for part whose links are to be retrieved
     * @return list of {@link PartData} that represent links
     */
    @GET
    @Path("/{id}/links")
    public Response getLinkedParts(@PathParam("id") long partId,
                                   @DefaultValue("CHILD") @QueryParam("linkType") LinkType linkType) {
        final String userId = requireUserId();
        EntryLinks entryLinks = new EntryLinks(userId, partId);
        return super.respond(entryLinks.get(linkType));
    }

    /**
     * Removes the linkId from id
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
     * @param partId   part to be linked
     * @param partData should essentially just contain the part Id or details for a new entry that should be created
     */
    @POST
    @Path("/{id}/links")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLink(@PathParam("id") long partId,
                               @DefaultValue("CHILD") @QueryParam("linkType") LinkType type,
                               PartData partData) {
        String userId = getUserId();
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
        Entries entries = new Entries(userId);
        List<Long> arrayList = new ArrayList<>();
        for (Number id : entryIds)
            arrayList.add(id.longValue());
        boolean success = entries.updateVisibility(arrayList, visibility);
        return super.respond(success);
    }

    @GET
    @Path("/{id}/annotations/auto")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAutoAnnotations(@PathParam("id") long partId,
                                       @QueryParam("ownerFeatures") boolean ownerFeatures) {
        String userId = requireUserId();
        log(userId, "requesting auto annotations for entry " + partId);
        Annotations annotations = new Annotations(userId);
        return super.respond(annotations.generate(partId, ownerFeatures));
    }
}
