package org.jbei.ice.lib.entry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.jbei.ice.ApplicationController;
import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.DNASequence;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.PartAttachment;
import org.jbei.ice.lib.vo.SequencePartTransfer;
import org.jbei.ice.lib.vo.TraceSequenceAttachment;
import org.jbei.ice.servlet.InfoToModelFactory;
import org.jbei.ice.servlet.ModelToInfoFactory;

import org.apache.commons.io.IOUtils;

/**
 * Handles transfer of entries to other registries
 * and those that have been transferred to this registry
 *
 * @author Hector Plahar
 */
public class EntryTransfers {

    private final EntryDAO dao;

    public EntryTransfers() {
        this.dao = DAOFactory.getEntryDAO();
    }

    public void transferEntries(Account account, ArrayList<Long> ids, ArrayList<String> sites)
            throws ControllerException {
        if (account.getType() != AccountType.ADMIN)
            return;

        Logger.info(account.getEmail() + ": requesting transfer of " + ids.size() + " entries");

        // retrieve entries
        SequenceController sequenceController = new SequenceController();
        AttachmentController attachmentController = new AttachmentController();
        ArrayList<SequencePartTransfer> parts = new ArrayList<>();

        for (long id : ids) {
            try {
                PartData partData;
                SequencePartTransfer part = new SequencePartTransfer();

                try {
                    Entry entry = dao.get(id);
                    partData = ModelToInfoFactory.getInfo(entry);
                    part.setPart(partData);
                    Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);

                    // convert sequence
                    if (sequence != null) {
                        part.setSequence(sequenceController.sequenceToDNASequence(sequence));
                    }

                    // attachments
                    if (attachmentController.hasAttachment(entry)) {
                        ArrayList<PartAttachment> attachments = new ArrayList<>();
                        for (Attachment attachment : attachmentController.getByEntry(account.getEmail(), entry)) {
                            PartAttachment partAttachment = new PartAttachment();
                            partAttachment.setName(attachment.getFileName());
                            partAttachment.setDescription(attachment.getDescription());

                            DataSource source = new FileDataSource(attachmentController.getFile(account, attachment));
                            partAttachment.setAttachmentData(new DataHandler(source));
                            attachments.add(partAttachment);
                        }
                        part.setAttachments(attachments);
                    }

                    // trace sequences
                    SequenceAnalysisController controller = new SequenceAnalysisController();
                    List<TraceSequence> traceSequences = controller.getTraceSequences(entry);
                    if (traceSequences != null) {
                        ArrayList<TraceSequenceAttachment> attachments = new ArrayList<>();
                        for (TraceSequence traceSequence : traceSequences) {
                            TraceSequenceAttachment attachment = new TraceSequenceAttachment();
                            attachment.setFilename(traceSequence.getFilename());
                            File file = controller.getFile(traceSequence);
                            if (file == null || !file.exists()) {
                                Logger.error("Could not find trace sequence file " + traceSequence.getFileId());
                                continue;
                            }
                            DataHandler dataHandler = new DataHandler(new FileDataSource(file));
                            attachment.setSequenceData(dataHandler);
                            attachment.setDepositor(traceSequence.getDepositor());
                            attachments.add(attachment);
                        }
                        part.setTraceSequences(attachments);
                    }
                } catch (DAOException | PermissionException e) {
                    Logger.error(e);
                    continue;
                }

                parts.add(part);
            } catch (ControllerException e) {
                Logger.error(e);
            }
        }

        String siteUrl = new ConfigurationController().getPropertyValue(ConfigurationKey.URI_PREFIX);

        for (String url : sites) {
            // TODO
//            IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);
//            if (api == null) {
//                Logger.error("Could not retrieve api for " + url + ". Transfer aborted");
//                continue;
//            }
//
//            try {
//                String apiKey = new WoRController().getApiKey(url);
//                api.uploadPartsWithSequences(siteUrl, apiKey, parts);
//            } catch (ServiceException e) {
//                Logger.error(e);
//            }
        }
    }

    public boolean recordParts(ArrayList<SequencePartTransfer> parts) {
        if (parts == null)
            return false;

        try {
            for (SequencePartTransfer part : parts) {
                Entry entry = InfoToModelFactory.infoToEntry(part.getPart());
                entry.setVisibility(Visibility.TRANSFERRED.getValue());

                // always assign new record id to uploaded part and maintain any existing as legacy version
                String newRecordId = Utils.generateUUID();
                if (entry.getRecordId() == null || entry.getRecordId().trim().isEmpty()) {
                    entry.setRecordId(newRecordId);
                    entry.setVersionId(newRecordId);
                } else {
                    entry.setVersionId(entry.getRecordId());
                    entry.setRecordId(newRecordId);
                }

                entry = dao.create(entry);

                // check attachments
                if (part.getAttachments() != null && !part.getAttachments().isEmpty()) {
                    for (PartAttachment partAttachment : part.getAttachments()) {
                        DataHandler handler = partAttachment.getAttachmentData();
                        Attachment attachment = new Attachment();
                        attachment.setEntry(entry);
                        attachment.setDescription(partAttachment.getDescription());
                        attachment.setFileName(partAttachment.getName());
                        AttachmentController attachmentController = new AttachmentController();
                        try {
                            attachmentController.save(null, attachment, handler.getInputStream());
                        } catch (IOException e) {
                            Logger.error(e);
                        }
                    }
                }

                // check sequence
                FeaturedDNASequence featuredDNASequence = part.getSequence();
                if (featuredDNASequence != null) {
                    Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);
                    sequence.setEntry(entry);
                    new SequenceController().saveSequence(sequence);
                    ApplicationController.scheduleBlastIndexRebuildTask(true);
                }

                // check trace sequences
                SequenceAnalysisController analysisController = new SequenceAnalysisController();
                ArrayList<TraceSequenceAttachment> traceSequences = part.getTraceSequences();
                if (traceSequences != null) {
                    for (TraceSequenceAttachment traceSequence : traceSequences) {
                        try {
                            byte[] bytes = IOUtils.toByteArray(traceSequence.getData().getInputStream());
                            DNASequence dnaSequence = analysisController.parse(bytes);
                            if (dnaSequence == null || dnaSequence.getSequence() == null) {
                                Logger.error("Could not parse trace sequence file " + traceSequence.getFilename());
                                continue;
                            }

                            analysisController.uploadTraceSequence(entry, traceSequence.getFilename(),
                                                                   traceSequence.getDepositor(),
                                                                   dnaSequence.getSequence().toLowerCase(),
                                                                   new ByteArrayInputStream(bytes));
                        } catch (IOException io) {
                            Logger.error(io);
                        }
                    }
                }
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
            return false;
        }

        return true;
    }

    public ArrayList<PartData> getTransferredParts(Account account) throws ControllerException {
        if (account.getType() != AccountType.ADMIN)
            return null;

        try {
            Set<Entry> entries = dao.retrieveTransferredEntries();
            if (entries == null)
                return null;

            ArrayList<PartData> data = new ArrayList<>();
            for (Entry entry : entries) {
                data.add(ModelToInfoFactory.createTableViewData(null, entry, false));
            }
            return data;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public boolean processTransferredParts(Account account, ArrayList<Long> partIds, boolean accept)
            throws ControllerException {
        if (account.getType() != AccountType.ADMIN)
            return false;

        Visibility visibility = accept ? Visibility.OK : Visibility.DELETED;
        try {
            dao.setEntryVisibility(partIds, visibility.getValue());
            return true;
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }
}
