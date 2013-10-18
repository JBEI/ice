package org.jbei.ice.lib.entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.Visibility;
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.PartAttachment;
import org.jbei.ice.lib.vo.SequencePartTransfer;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.server.ModelToInfoFactory;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;
import org.jbei.ice.services.webservices.ServiceException;

/**
 * Handles transfer of entries to other registries
 * and those that have been transferred to this registry
 *
 * @author Hector Plahar
 */
public class EntryTransfers {

    private final EntryDAO dao;

    public EntryTransfers() {
        this.dao = new EntryDAO();
    }

    public void transferEntries(Account account, ArrayList<Long> ids, ArrayList<String> sites)
            throws ControllerException {
        if (account.getType() != AccountType.ADMIN)
            return;

        Logger.info(account.getEmail() + ": requesting transfer of " + ids.size() + " entries");

        // retrieve entries
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        AttachmentController attachmentController = ControllerFactory.getAttachmentController();
        ArrayList<SequencePartTransfer> parts = new ArrayList<>();

        for (long id : ids) {
            try {
                PartData partData;
                String sequenceString = null;
                SequencePartTransfer part = new SequencePartTransfer();

                try {
                    Entry entry = dao.get(id);
                    partData = ModelToInfoFactory.getInfo(entry);
                    part.setPart(partData);
                    Sequence sequence = sequenceController.getByEntry(entry);

                    // convert sequence
                    if (sequence != null) {
                        part.setSequence(sequenceController.sequenceToDNASequence(sequence));
                    }

                    // attachments
                    if (attachmentController.hasAttachment(entry)) {
                        ArrayList<PartAttachment> attachments = new ArrayList<>();
                        for (Attachment attachment : attachmentController.getByEntry(account, entry)) {
                            PartAttachment partAttachment = new PartAttachment();
                            partAttachment.setName(attachment.getFileName());
                            partAttachment.setDescription(attachment.getDescription());

                            DataSource source = new FileDataSource(attachmentController.getFile(account, attachment));
                            partAttachment.setAttachmentData(new DataHandler(source));
                            attachments.add(partAttachment);
                        }
                        part.setAttachments(attachments);
                    }

                    // TODO : sequence controllers

                } catch (DAOException | PermissionException e) {
                    Logger.error(e);
                    continue;
                }

                parts.add(part);
            } catch (ControllerException e) {
                Logger.error(e);
            }
        }

        String siteUrl = ControllerFactory.getConfigurationController().getPropertyValue(ConfigurationKey.URI_PREFIX);

        for (String url : sites) {
            IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);
            if (api == null) {
                Logger.error("Could not retrieve api for " + url + ". Transfer aborted");
                continue;
            }

            try {
                String apiKey = ControllerFactory.getWebController().getApiKey(url);
                api.uploadPartsWithSequences(siteUrl, apiKey, parts);
            } catch (ServiceException e) {
                Logger.error(e);
            }
        }
    }

    protected String getSequenceGenbank(Sequence sequence) {
        Entry entry = sequence.getEntry();

        SequenceController sequenceController = ControllerFactory.getSequenceController();
        GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getName());
        genbankFormatter.setCircular((entry instanceof Plasmid) ? ((Plasmid) entry).getCircular() : false); // TODO

        try {
            return sequenceController.compose(sequence, genbankFormatter);
        } catch (ControllerException e) {
            Logger.error("Failed to generate genbank file for download!", e);
            return null;
        }
    }

    public boolean recordParts(ArrayList<SequencePartTransfer> parts) {
        if (parts == null)
            return false;

        try {
            for (SequencePartTransfer part : parts) {
                Entry entry = InfoToModelFactory.infoToEntry(part.getPart());
                entry.setVisibility(Visibility.TRANSFERRED.getValue());
                entry.setPartNumber(ControllerFactory.getEntryController().getNextPartNumber());
                if (entry.getRecordId() == null)
                    entry.setRecordId(Utils.generateUUID());

                if (entry.getVersionId() == null)
                    entry.setVersionId(entry.getRecordId());

                try {
                    entry = dao.save(entry);
                } catch (DAOException e) {
                    Logger.error(e);
                    continue;
                }

                // check attachments
                if (part.getAttachments() != null && !part.getAttachments().isEmpty()) {
                    for (PartAttachment partAttachment : part.getAttachments()) {
                        DataHandler handler = partAttachment.getAttachmentData();
                        Attachment attachment = new Attachment();
                        attachment.setEntry(entry);
                        attachment.setDescription(partAttachment.getDescription());
                        attachment.setFileName(partAttachment.getName());
                        AttachmentController attachmentController = ControllerFactory.getAttachmentController();
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
                    ControllerFactory.getSequenceController().saveSequence(sequence);
                    ApplicationController.scheduleBlastIndexRebuildTask(true);
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
                data.add(ModelToInfoFactory.createTableViewData(entry, false));
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
