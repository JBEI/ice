package org.jbei.ice.services.rest;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.bulkupload.FileBulkUpload;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.entry.EntriesAsCSV;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.attachment.Attachments;
import org.jbei.ice.lib.entry.sequence.*;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.net.RemoteEntries;
import org.jbei.ice.lib.net.RemoteSequence;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ShotgunSequenceDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.ShotgunSequence;
import org.jbei.ice.storage.model.TraceSequence;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource for accessing files both locally and remotely
 *
 * @author Hector Plahar
 */
@Path("/file")
public class FileResource extends RestResource {

    private SequenceController sequenceController = new SequenceController();
    private Attachments attachments = new Attachments();

    @GET
    @Path("asset/{assetName}")
    public Response getAsset(@PathParam("assetName") final String assetName) {
        ConfigurationController configurationController = new ConfigurationController();
        File assetFile = configurationController.getUIAsset(assetName);
        if (assetFile == null)
            return super.respond(Response.Status.NOT_FOUND);
        return addHeaders(Response.ok(assetFile), assetFile.getName());
    }

    /**
     * @return Response with attachment info on uploaded file
     */
    @POST
    @Path("attachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@FormDataParam("file") InputStream fileInputStream,
                         @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        try {
            final String fileName = contentDispositionHeader.getFileName();
            final String fileId = Utils.generateUUID();
            final File attachmentFile = Paths.get(
                    Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                    Attachments.attachmentDirName, fileId).toFile();
            FileUtils.copyInputStreamToFile(fileInputStream, attachmentFile);
            final AttachmentInfo info = new AttachmentInfo();
            info.setFileId(fileId);
            info.setFilename(fileName);
            return Response.status(Response.Status.OK).entity(info).build();
        } catch (final IOException e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a temp file by fileId
     */
    @GET
    @Path("tmp/{fileId}")
    public Response getTmpFile(@PathParam("fileId") final String fileId,
                               @QueryParam("filename") String fileName) {
        final File tmpFile = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY), fileId).toFile();
        if (tmpFile == null || !tmpFile.exists()) {
            return super.respond(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(fileName))
            fileName = tmpFile.getName();

        return addHeaders(Response.ok(tmpFile), fileName);
    }

    @GET
    @Path("attachment/{fileId}")
    public Response getAttachment(@PathParam("fileId") String fileId) {
        String userId = requireUserId();
        try {
            ByteArrayWrapper wrapper = attachments.getAttachmentByFileId(userId, fileId);
            if (wrapper == null) {
                return respond(Response.Status.NOT_FOUND);
            }

            return addHeaders(Response.ok(wrapper.getBytes()), wrapper.getName());
        } catch (IOException e) {
            Logger.error(e);
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("remote/{id}/attachment/{fileId}")
    public Response getRemoteAttachment(@PathParam("id") long partnerId,
                                        @PathParam("fileId") String fileId,
                                        @QueryParam("sid") String sid) {
        String userId = getUserId(sessionId);
        RemoteEntries entries = new RemoteEntries();
        File file = entries.getPublicAttachment(userId, partnerId, fileId);
        if (file == null)
            return respond(Response.Status.NOT_FOUND);

        return addHeaders(Response.ok(file), "remoteAttachment");
    }

    @GET
    @Path("upload/{type}")
    public Response getUploadCSV(@PathParam("type") final String type,
                                 @QueryParam("link") final String linkedType) {
        EntryType entryAddType = EntryType.nameToType(type);
        EntryType linked;
        if (linkedType != null) {
            linked = EntryType.nameToType(linkedType);
        } else {
            linked = null;
        }

        final StreamingOutput stream = output -> {
            byte[] template = FileBulkUpload.getCSVTemplateBytes(entryAddType, linked,
                    "existing".equalsIgnoreCase(linkedType));
            ByteArrayInputStream input = new ByteArrayInputStream(template);
            ByteStreams.copy(input, output);
        };

        String filename = type.toLowerCase();
        if (linkedType != null) {
            filename += ("_" + linkedType.toLowerCase());
        }

        return addHeaders(Response.ok(stream), filename + "_csv_upload.csv");
    }

    @GET
    @Path("{partId}/sequence/{type}")
    public Response downloadSequence(
            @PathParam("partId") final long partId,
            @PathParam("type") final String downloadType,
            @DefaultValue("-1") @QueryParam("remoteId") long remoteId,
            @QueryParam("sid") String sid) {
        if (StringUtils.isEmpty(sessionId))
            sessionId = sid;

        final String userId = getUserId(sessionId);
        final ByteArrayWrapper wrapper;
        if (remoteId != -1) {
            RemoteSequence sequence = new RemoteSequence(remoteId, partId);
            wrapper = sequence.get(downloadType);
        } else {
            wrapper = sequenceController.getSequenceFile(userId, partId, SequenceFormat.fromString(downloadType));
        }

        StreamingOutput stream = output -> {
            final ByteArrayInputStream input = new ByteArrayInputStream(wrapper.getBytes());
            ByteStreams.copy(input, output);
        };

        return addHeaders(Response.ok(stream), wrapper.getName());
    }

    @GET
    @Path("trace/{fileId}")
    public Response getTraceSequenceFile(@PathParam("fileId") String fileId,
                                         @QueryParam("sid") String sid) {
        final SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();
        final TraceSequence traceSequence = sequenceAnalysisController.getTraceSequenceByFileId(fileId);
        if (traceSequence != null) {
            final File file = sequenceAnalysisController.getFile(traceSequence);
            return addHeaders(Response.ok(file), traceSequence.getFilename());
        }
        return Response.serverError().build();
    }

    @GET
    @Path("shotgunsequence/{fileId}")
    public Response getShotgunSequenceFile(@PathParam("fileId") String fileId) {
        ShotgunSequenceDAO dao = DAOFactory.getShotgunSequenceDAO();
        ShotgunSequence shotgunSequence = dao.getByFileId(fileId);

        try {
            final File file = dao.getFile(fileId);
            return addHeaders(Response.ok(file), shotgunSequence.getFilename());
        } catch (Exception e) {
            Logger.error(e);
            return Response.serverError().build();
        }
    }

    @GET
    @Produces("image/png")
    @Path("sbolVisual/{rid}")
    public Response getSBOLVisual(@PathParam("rid") String recordId) {
        final String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        final Entry entry = DAOFactory.getEntryDAO().getByRecordId(recordId);
        final Sequence sequence = entry.getSequence();
        final String hash = sequence.getFwdHash();
        final File png = Paths.get(tmpDir, hash + ".png").toFile();

        if (png.exists()) {
            return addHeaders(Response.ok(png), entry.getPartNumber() + ".png");
        }

        final URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
        if (uri != null) {
            try (final InputStream in = uri.toURL().openStream();
                 final OutputStream out = new FileOutputStream(png)) {
                ByteStreams.copy(in, out);
            } catch (IOException e) {
                Logger.error(e);
                return respond(false);
            }

            return addHeaders(Response.ok(png), entry.getPartNumber() + ".png");
        }
        return respond(false);
    }

    /**
     * this creates an entry if an id is not specified in the form data
     */
    @POST
    @Path("sequence")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadSequence(@FormDataParam("file") InputStream fileInputStream,
                                   @FormDataParam("entryRecordId") String recordId,
                                   @FormDataParam("entryType") String entryType,
                                   @FormDataParam("extractHierarchy") @DefaultValue("false") boolean extractHierarchy,
                                   @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        try {
            final String fileName = contentDispositionHeader.getFileName();
            String userId = getUserId();

            PartSequence partSequence;
            if (StringUtils.isEmpty(recordId)) {
                if (entryType == null) {
                    entryType = "PART";
                }
                EntryType type = EntryType.nameToType(entryType);
                partSequence = new PartSequence(userId, type);
            } else {
                partSequence = new PartSequence(userId, recordId);
            }

            SequenceInfo info = partSequence.parseSequenceFile(fileInputStream, fileName, extractHierarchy);
            if (info == null)
                throw new WebApplicationException(Response.serverError().build());
            return Response.status(Response.Status.OK).entity(info).build();
        } catch (Exception e) {
            Logger.error(e);
            ErrorResponse response = new ErrorResponse();
            response.setMessage(e.getMessage());
            throw new WebApplicationException(Response.serverError().entity(response).build());
        }
    }

    /**
     * Extracts the csv information and writes it to the temp dir and returns the file uuid. Then
     * the client is expected to make another rest call with the uuid in a separate window. This
     * workaround is due to not being able to download files using XHR or sumsuch
     */
    @POST
    @Path("csv")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadCSV(@QueryParam("sequenceFormats") final List<String> sequenceFormats,
                                @QueryParam("entryFields") final List<String> fields,
                                EntrySelection selection) {
        String userId = super.requireUserId();
        EntriesAsCSV entriesAsCSV = new EntriesAsCSV(userId, sequenceFormats.toArray(new String[sequenceFormats.size()]));
        List<EntryField> entryFields = new ArrayList<>();
        try {
            if (fields != null) {
                entryFields.addAll(fields.stream().map(EntryField::fromString).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            Logger.error(e);
        }

        boolean success = entriesAsCSV.setSelectedEntries(selection,
                entryFields.toArray(new EntryField[entryFields.size()]));
        if (!success)
            return super.respond(false);

        final File file = entriesAsCSV.getFilePath().toFile();
        if (file.exists()) {
            return Response.ok(new Setting("key", file.getName())).build();
        }

        return Response.serverError().build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("entries")
    public Response getEntriesInFile(@FormDataParam("file") InputStream fileInputStream,
                                     @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        String userId = requireUserId();
        try {
            Entries entries = new Entries(userId);
            return super.respond(entries.validateEntries(fileInputStream));
        } catch (IOException e) {
            Logger.error(e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
