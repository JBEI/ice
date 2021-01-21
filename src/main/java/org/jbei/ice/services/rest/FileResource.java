package org.jbei.ice.services.rest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbei.ice.lib.bulkupload.FileBulkUpload;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationSettings;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.EntryFieldLabel;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.entry.EntriesAsCSV;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.attachment.Attachments;
import org.jbei.ice.lib.entry.sequence.InputStreamWrapper;
import org.jbei.ice.lib.entry.sequence.PartSequence;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.lib.entry.sequence.Sequences;
import org.jbei.ice.lib.entry.sequence.analysis.TraceSequences;
import org.jbei.ice.lib.net.RemoteEntries;
import org.jbei.ice.lib.net.RemoteSequence;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ShotgunSequenceDAO;
import org.jbei.ice.storage.model.ShotgunSequence;
import org.jbei.ice.storage.model.TraceSequence;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

    private final Attachments attachments = new Attachments();

    @GET
    @Path("asset/{assetName}")
    public Response getAsset(@PathParam("assetName") final String assetName) {
        ConfigurationSettings settings = new ConfigurationSettings();
        File assetFile = settings.getUIAsset(assetName);
        if (assetFile == null)
            return super.respond(Response.Status.NOT_FOUND);
        return addHeaders(Response.ok(assetFile), assetFile.getName());
    }

    @GET
    @Path("/exports/{fileId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadExportedFile(@PathParam("fileId") String fileId) {
        String userId = requireUserId();
        final java.nio.file.Path tmpFile = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        String fileName = userId + "_" + fileId + "_export-data.zip";
        if (!Files.exists(Paths.get(tmpFile.toString(), "export", fileName)))
            return super.respond(Response.Status.NOT_FOUND);

        StreamingOutput stream = output -> {
            java.nio.file.Path file = Paths.get(tmpFile.toString(), "export", fileName);
            final ByteArrayInputStream input = new ByteArrayInputStream(FileUtils.readFileToByteArray(file.toFile()));
            IOUtils.copy(input, output);
        };
        return addHeaders(Response.ok(stream), "ice-export-data.zip");
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
                               @QueryParam("filename") String fileName,
                               @DefaultValue("false") @QueryParam("delete") boolean delete) {
        final File tmpFile = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY), fileId).toFile();
        if (!tmpFile.exists()) {
            return super.respond(Response.Status.NOT_FOUND);
        }
        if (StringUtils.isEmpty(fileName))
            fileName = tmpFile.getName();

        if (delete)
            tmpFile.deleteOnExit();

        return addHeaders(Response.ok(tmpFile), fileName);
    }

    @GET
    @Path("attachment/{fileId}")
    public Response getAttachment(@PathParam("fileId") String fileId) {
        String userId = requireUserId();
        try {
            InputStreamWrapper wrapper = attachments.getAttachmentByFileId(userId, fileId);
            if (wrapper == null) {
                return respond(Response.Status.NOT_FOUND);
            }

            StreamingOutput stream = output -> IOUtils.copy(wrapper.getInputStream(), output);

            return addHeaders(Response.ok(stream), wrapper.getName());
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

        final byte[] template = FileBulkUpload.getCSVTemplateBytes(entryAddType, linked,
                "existing".equalsIgnoreCase(linkedType));

        final StreamingOutput stream = output -> {
            ByteArrayInputStream input = new ByteArrayInputStream(template);
            IOUtils.copy(input, output);
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
            @PathParam("partId") final String partId,
            @PathParam("type") final String downloadType,
            @DefaultValue("-1") @QueryParam("remoteId") long remoteId,
            @QueryParam("sid") String sid) {
        if (StringUtils.isEmpty(sessionId))
            sessionId = sid;

        final String userId = getUserId(sessionId);
        if (remoteId != -1) {
            RemoteSequence sequence = new RemoteSequence(remoteId, Long.decode(partId));
            final InputStreamWrapper wrapper = sequence.get(downloadType);
            StreamingOutput stream = output -> IOUtils.copy(wrapper.getInputStream(), output);

            return addHeaders(Response.ok(stream), wrapper.getName());
        } else {
            InputStreamWrapper wrapper = new PartSequence(userId, partId).toFile(SequenceFormat.fromString(downloadType), true);
            StreamingOutput stream = output -> IOUtils.copy(wrapper.getInputStream(), output);
            return addHeaders(Response.ok(stream), wrapper.getName());
        }
    }

    @GET
    @Path("trace/{fileId}")
    public Response getTraceSequenceFile(@PathParam("fileId") String fileId, @QueryParam("sid") String sid) {
        TraceSequences traceSequences = new TraceSequences();
        final TraceSequence traceSequence = traceSequences.getTraceSequenceByFileId(fileId);
        if (traceSequence != null) {
            final File file = traceSequences.getFile(traceSequence);
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
        } catch (IOException e) {
            Logger.error(e);
            ErrorResponse response = new ErrorResponse();
            response.setMessage(e.getMessage());
            throw new WebApplicationException(Response.serverError().entity(response).build());
        }
    }

    /**
     * Create a model of the uploaded sequence file. Note that this does not associate the sequence
     * with any existing entry. It just parses the uploaded file
     */
    @POST
    @Path("sequence/model")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSequenceModel(@FormDataParam("file") InputStream fileInputStream,
                                        @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        final String fileName = contentDispositionHeader.getFileName();
        Sequences sequences = new Sequences(requireUserId());
        try {
            return super.respond(sequences.parseSequence(fileInputStream, fileName));
        } catch (InvalidFormatParserException e) {
            throw new WebApplicationException(e.getMessage());
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
        EntriesAsCSV entriesAsCSV = new EntriesAsCSV(userId, sequenceFormats.toArray(new String[0]));
        List<EntryFieldLabel> entryFieldLabels = new ArrayList<>();
        try {
            if (fields != null && !fields.isEmpty()) {
                entryFieldLabels.addAll(fields.stream().map(EntryFieldLabel::fromString).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            Logger.error(e);
        }

        boolean success = entriesAsCSV.setSelectedEntries(selection,
                entryFieldLabels.toArray(new EntryFieldLabel[0]));
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
                                     @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
                                     @DefaultValue("false") @QueryParam("checkName") boolean checkName) {
        String userId = requireUserId();
        try {
            Entries entries = new Entries(userId);
            return super.respond(entries.validateEntries(fileInputStream, checkName));
        } catch (IOException e) {
            Logger.error(e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
