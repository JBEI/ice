package org.jbei.ice.services.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.utils.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * @author Hector Plahar
 */
@Path("/file")
public class FileResource extends RestResource {

    private SequenceController sequenceController = new SequenceController();

    @POST
    @Path("attachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            getUserIdFromSessionHeader(sessionId);
            String fileName = contentDispositionHeader.getFileName();
            String fileId = Utils.generateUUID();
            File attachmentFile = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                                            AttachmentController.attachmentDirName, fileId).toFile();
            FileUtils.copyInputStreamToFile(fileInputStream, attachmentFile);
            AttachmentInfo info = new AttachmentInfo();
            info.setFileId(fileId);
            info.setFilename(fileName);
            return Response.status(Response.Status.OK).entity(info).build();
        } catch (IOException e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("attachment/{fileId}")
    public Response getAttachment(@PathParam("fileId") String fileId,
            @QueryParam("sid") String sid,
            @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            if (StringUtils.isEmpty(sessionId))
                sessionId = sid;

            String userId = getUserIdFromSessionHeader(sessionId);

            AttachmentController controller = new AttachmentController();
            Attachment attachment = controller.getAttachmentByFileId(fileId);
            if (attachment == null)
                return null;

            Account account = DAOFactory.getAccountDAO().getByEmail(userId);
            File file = controller.getFile(account, attachment);
            Response.ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"");
            return response.build();
        } catch (Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("trace/{fileId}")
    public Response getTraceSequenceFile(@PathParam("fileId") String fileId,
            @QueryParam("sid") String sid,
            @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();
            TraceSequence traceSequence = sequenceAnalysisController.getTraceSequenceByFileId(fileId);
            if (traceSequence != null) {
                File file = sequenceAnalysisController.getFile(traceSequence);
                Response.ResponseBuilder response = Response.ok(file);
                response.header("Content-Disposition", "attachment; filename=\"" + traceSequence.getFilename() + "\"");
                return response.build();
            }
            return Response.serverError().build();
        } catch (Exception e) {
            Logger.error(e);
            return Response.serverError().build();
        }
    }

    @GET
    @Produces("image/png")
    @Path("sbolVisual/{rid}")
    public Response getSBOLVisual(@PathParam("rid") String recordId,
            @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
            Entry entry = DAOFactory.getEntryDAO().getByRecordId(recordId);
            Sequence sequence = entry.getSequence();
            String hash = sequence.getFwdHash();
            String fileId;

            if (Paths.get(tmpDir, hash + ".png").toFile().exists()) {
                fileId = (hash + ".png");
                File file = Paths.get(tmpDir, fileId).toFile();

                Response.ResponseBuilder response = Response.ok(file);
                response.header("Content-Disposition", "attachment; filename=" + entry.getPartNumber() + ".png");
                return response.build();
            } else {
                URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
                if (uri != null) {
                    IOUtils.copy(uri.toURL().openStream(),
                                 new FileOutputStream(tmpDir + File.separatorChar + hash + ".png"));
                    fileId = (hash + ".png");
                    File file = Paths.get(tmpDir, fileId).toFile();

                    Response.ResponseBuilder response = Response.ok(file);
                    response.header("Content-Disposition", "attachment; filename=" + entry.getPartNumber() + ".png");
                    return response.build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // this creates an entry if an id is not specified in the form data
    @POST
    @Path("sequence")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadSequence(@FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("entryRecordId") String recordId,
            @FormDataParam("entryType") String entryType,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
            @HeaderParam("X-ICE-Authentication-SessionId") String sessionId) {
        try {
            String fileName = contentDispositionHeader.getFileName();
            String userId = super.getUserIdFromSessionHeader(sessionId);
            String sequence = IOUtils.toString(fileInputStream);
            SequenceInfo sequenceInfo = sequenceController.parseSequence(userId, recordId, entryType, sequence);
            if (sequenceInfo == null)
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            sequenceInfo.setFilename(fileName);
            return Response.status(Response.Status.OK).entity(sequenceInfo).build();
        } catch (Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
