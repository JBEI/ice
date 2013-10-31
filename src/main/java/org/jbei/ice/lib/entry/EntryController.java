package org.jbei.ice.lib.entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.StorageController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.TraceSequenceDAO;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Comment;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.StorageInfo;
import org.jbei.ice.lib.shared.dto.comment.UserComment;
import org.jbei.ice.lib.shared.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.Visibility;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.lib.vo.PartAttachment;
import org.jbei.ice.lib.vo.PartTransfer;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.server.ModelToInfoFactory;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;
import org.jbei.ice.services.webservices.ServiceException;

import org.apache.commons.io.IOUtils;

/**
 * ABI to manipulate {@link org.jbei.ice.lib.entry.model.Entry}s.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class EntryController {

    private EntryDAO dao;
    private CommentDAO commentDAO;
    private PermissionsController permissionsController;
    private AccountController accountController;
    private SequenceController sequenceController;

    public EntryController() {
        dao = new EntryDAO();
        commentDAO = new CommentDAO();
        permissionsController = ControllerFactory.getPermissionController();
        accountController = ControllerFactory.getAccountController();
        sequenceController = ControllerFactory.getSequenceController();
    }

    public Set<String> getMatchingAutoCompleteField(AutoCompleteField field, String token, int limit)
            throws ControllerException {
        token = token.replaceAll("'", "");
        try {
            Set<String> results;
            switch (field) {
                case SELECTION_MARKERS:
                    results = dao.getMatchingSelectionMarkers(token, limit);
                    break;

                case ORIGIN_OF_REPLICATION:
                    results = dao.getMatchingOriginOfReplication(token, limit);
                    break;

                case PROMOTERS:
                    results = dao.getMatchingPromoters(token, limit);
                    break;

                case REPLICATES_IN:
                    results = dao.getMatchingReplicatesIn(token, limit);
                    break;

                case PLASMID_NAME:
                    results = dao.getMatchingPlasmidPartNumbers(token, limit);
                    break;

                default:
                    results = new HashSet<>();
            }

            // process to remove commas
            HashSet<String> individualResults = new HashSet<>();
            for (String result : results) {
                for (String split : result.split(",")) {
                    individualResults.add(split.trim());
                }
            }
            return individualResults;
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    /**
     * Retrieves the IDs of all part records in the system
     *
     * @return list of ids
     * @throws ControllerException on DAOException retrieving the IDs
     */
    public LinkedList<Long> getAllEntryIds() throws ControllerException {
        try {
            return dao.getAllEntryIds();
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public String getEntrySummary(long id) throws ControllerException {
        try {
            return dao.getEntrySummary(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Entry createStrainWithPlasmid(Account account, Entry strain, Entry plasmid,
            ArrayList<AccessPermission> accessPermissions) throws ControllerException {
        if (strain == null || plasmid == null)
            throw new ControllerException("Cannot create null entries");

        plasmid = createEntry(account, plasmid, accessPermissions);
        strain = createEntry(account, strain, accessPermissions);
        strain.getLinkedEntries().add(plasmid);
        try {
            return dao.update(strain);
        } catch (DAOException de) {
            Logger.error(de);
            throw new ControllerException(de);
        }
    }

    /**
     * Generate the next part number string using system settings.
     *
     * @return The next part number.
     * @throws ControllerException
     */
    private String getNextPartNumber() throws ControllerException {
        try {
            return dao.generateNextPartNumber(Utils.getConfigValue(ConfigurationKey.PART_NUMBER_PREFIX),
                                              Utils.getConfigValue(ConfigurationKey.PART_NUMBER_DELIMITER),
                                              Utils.getConfigValue(ConfigurationKey.PART_NUMBER_DIGITAL_SUFFIX));
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    /**
     * creates entry and assigns read permissions to all public groups that user creating the entry is a member of
     *
     * @param account account for user creating entry
     * @param entry   entry being created
     * @return created entry
     * @throws ControllerException on exception creating the entry
     */
    public Entry createEntry(Account account, Entry entry) throws ControllerException {
        ArrayList<AccessPermission> accessPermissions = permissionsController.getDefaultPermissions(account);
        return createEntry(account, entry, accessPermissions);
    }

    /**
     * Create an entry in the database.
     * <p/>
     * Generates a new Part Number, the record id (UUID), version id, and timestamps.
     * Optionally set the record globally visible or schedule an index rebuild.
     *
     * @param account           account of user creating entry
     * @param entry             entry record being created
     * @param accessPermissions list of permissions to associate with created entry
     * @return entry that was saved in the database.
     * @throws ControllerException
     */
    public Entry createEntry(Account account, Entry entry, ArrayList<AccessPermission> accessPermissions)
            throws ControllerException {
        entry.setPartNumber(getNextPartNumber());
        entry.setRecordId(Utils.generateUUID());
        entry.setVersionId(entry.getRecordId());
        entry.setCreationTime(Calendar.getInstance().getTime());
        entry.setModificationTime(entry.getCreationTime());
        entry.setOwner(account.getFullName());
        entry.setOwnerEmail(account.getEmail());

        if (entry.getSelectionMarkers() != null) {
            for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
                selectionMarker.setEntry(entry);
            }
        }

        if (entry.getLinks() != null) {
            for (Link link : entry.getLinks()) {
                link.setEntry(entry);
            }
        }

        if (entry.getStatus() == null)
            entry.setStatus("");

        if (entry.getBioSafetyLevel() == null)
            entry.setBioSafetyLevel(0);

        try {
            entry = dao.save(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        // add write permissions for owner
        AccessPermission access = new AccessPermission(AccessPermission.Article.ACCOUNT, account.getId(),
                                                       AccessPermission.Type.WRITE_ENTRY, entry.getId(),
                                                       account.getFullName());
        permissionsController.addPermission(account, access);

        if (accessPermissions != null) {
            for (AccessPermission accessPermission : accessPermissions) {
                accessPermission.setTypeId(entry.getId());
                permissionsController.addPermission(account, accessPermission);
            }
        }

        if (sequenceController.hasSequence(entry.getId())) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }

        return entry;
    }

    public PartData createPart(Account account, PartData part) throws ControllerException {
        Entry entry = InfoToModelFactory.infoToEntry(part);
        if (part.getLinkedParts() != null && part.getLinkedParts().size() > 0) {
            for (PartData data : part.getLinkedParts()) {
                try {
                    Entry linked = dao.getByPartNumber(data.getPartId());
                    if (linked == null)
                        continue;

                    if (!permissionsController.hasReadPermission(account, linked)) {
                        continue;
                    }

                    entry.getLinkedEntries().add(linked);
                } catch (DAOException e) {
                    Logger.error(e);
                }
            }
        }
        SampleController sampleController = ControllerFactory.getSampleController();
        StorageController storageController = ControllerFactory.getStorageController();
        ArrayList<SampleStorage> sampleMap = part.getSampleStorage();

        if (part.getInfo() != null) {
            // check if enclosed already exists
            Entry enclosed = get(account, part.getInfo().getId());
            if (enclosed == null) {
                enclosed = InfoToModelFactory.infoToEntry(part.getInfo());
                Entry created = createStrainWithPlasmid(account, entry, enclosed, part.getAccessPermissions());
                part.setRecordId(created.getRecordId());
            } else {
                // already exists, create strain and link
                updatePart(account, part.getInfo());
                enclosed = get(account, part.getInfo().getId());
                entry = createEntry(account, entry, part.getAccessPermissions());
                entry.getLinkedEntries().add(enclosed);
                try {
                    dao.update(entry);
                } catch (DAOException e) {
                    Logger.error(e);
                }
            }
        } else {
            entry = createEntry(account, entry, part.getAccessPermissions());
            part.setRecordId(entry.getRecordId());
        }
        if (sampleMap != null) {
            for (SampleStorage sampleStorage : sampleMap) {
                PartSample partSample = sampleStorage.getPartSample();
                LinkedList<StorageInfo> locations = sampleStorage.getStorageList();

                Sample sample = sampleController.createSample(partSample.getLabel(),
                                                              account.getEmail(), partSample.getNotes());
                sample.setEntry(entry);

                if (locations == null || locations.isEmpty()) {
                    // create sample, but not location
                    try {
                        Logger.info("Creating sample without location");
                        sampleController.saveSample(account, sample);
                    } catch (PermissionException e) {
                        Logger.warn(e.getMessage());
                        sample = null;
                    } catch (ControllerException e) {
                        Logger.error(e);
                        sample = null;
                    }
                } else {
                    // create sample and location
                    String[] labels = new String[locations.size()];
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < labels.length; i++) {
                        labels[i] = locations.get(i).getDisplay();
                        sb.append(labels[i]);
                        if (i - 1 < labels.length)
                            sb.append("/");
                    }

                    Logger.info("Creating sample with locations " + sb.toString());
                    Storage storage;
                    try {
                        Storage scheme = storageController.get(Long.parseLong(partSample.getLocationId()), false);
                        storage = storageController.getLocation(scheme, labels);
                        storage = storageController.update(storage);
                        sample.setStorage(storage);
                    } catch (NumberFormatException | ControllerException e) {
                        Logger.error(e);
                        continue;
                    }
                }

                if (sample != null) {
                    try {
                        sampleController.saveSample(account, sample);
                    } catch (ControllerException e) {
                        Logger.error(e);
                    } catch (PermissionException ce) {
                        Logger.warn(ce.getMessage());
                    }
                }
            }
        }

        // save attachments
        if (part.getAttachments() != null) {
            AttachmentController attachmentController = ControllerFactory.getAttachmentController();
            String attDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY) + File.separator
                    + AttachmentController.attachmentDirName;
            for (AttachmentInfo attachmentInfo : part.getAttachments()) {
                Attachment attachment = new Attachment();
                attachment.setEntry(entry);
                attachment.setDescription(attachmentInfo.getDescription());
                attachment.setFileName(attachmentInfo.getFilename());
                File file = new File(attDir + File.separator + attachmentInfo.getFileId());
                if (!file.exists())
                    continue;
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    attachmentController.save(account, attachment, inputStream);
                } catch (FileNotFoundException e) {
                    Logger.warn(e.getMessage());
                }
            }
        }

        part.setId(entry.getId());
        return part;
    }

    public void transferEntries(Account account, ArrayList<Long> ids, ArrayList<String> sites)
            throws ControllerException {
        if (!ControllerFactory.getAccountController().isAdministrator(account))
            return;

        Logger.info(account.getEmail() + ": requesting transfer of " + ids.size() + " entries");

        // retrieve entries
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        AttachmentController attachmentController = ControllerFactory.getAttachmentController();
        ArrayList<PartTransfer> parts = new ArrayList<>();

        for (long id : ids) {
            try {
                PartData partData;
                String sequenceString = null;
                PartTransfer part = new PartTransfer();

                try {
                    Entry entry = dao.get(id);
                    partData = ModelToInfoFactory.getInfo(entry);
                    part.setPart(partData);
                    Sequence sequence = sequenceController.getByEntry(entry);

                    // this uses the uploaded file which contains the annotations. If there is none (or created with
                    // vector editor) then the raw sequence is sent
                    if (sequence != null) {
                        sequenceString = sequence.getSequenceUser();
                        if (sequenceString == null || sequenceString.isEmpty())
                            sequenceString = sequence.getSequence();
                    }

                    PartAttachment sequenceAttachment = new PartAttachment();
                    DataHandler data = new DataHandler(sequenceString.getBytes(), "application/octet-stream");
                    sequenceAttachment.setAttachmentData(data);
                    part.setSequence(sequenceAttachment);

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
                api.uploadParts(siteUrl, parts);
            } catch (ServiceException e) {
                Logger.error(e);
            }
        }
    }

    // NOTE that this method returns the plasmid record id, not the strain
    public String createStrainWithPlasmid(Account account, PartTransfer strainTransfer, PartTransfer plasmidTransfer,
            ArrayList<AccessPermission> accessPermissions) throws ControllerException {
        // create strain
        Entry strain = InfoToModelFactory.infoToEntry(strainTransfer.getPart());
        Entry plasmid = InfoToModelFactory.infoToEntry(plasmidTransfer.getPart());

        strain = createStrainWithPlasmid(account, strain, plasmid, accessPermissions);

        // check attachments
        if (strainTransfer.getAttachments() != null) {
            for (PartAttachment partAttachment : strainTransfer.getAttachments()) {
                DataHandler handler = partAttachment.getAttachmentData();
                Attachment attachment = new Attachment();
                attachment.setEntry(strain);
                attachment.setDescription(partAttachment.getDescription());
                attachment.setFileName(partAttachment.getName());
                AttachmentController attachmentController = ControllerFactory.getAttachmentController();
                try {
                    attachmentController.save(account, attachment, handler.getInputStream());
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
        }

        // check sequence
        if (strainTransfer.getSequence() != null) {
            String sequenceString;
            try {
                sequenceString = IOUtils.toString(strainTransfer.getSequence().getAttachmentData().getInputStream());
                IDNASequence dnaSequence = SequenceController.parse(sequenceString);
                if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
                    Logger.error("Couldn't parse sequence file!");
                } else {
                    Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
                    sequence.setSequenceUser(sequenceString);
                    sequence.setEntry(strain);
                    sequenceController.saveSequence(sequence);
                }
            } catch (IOException e) {
                Logger.error(e);
            }
        }

        // check attachments
        if (plasmidTransfer.getAttachments() != null) {
            for (PartAttachment partAttachment : plasmidTransfer.getAttachments()) {
                DataHandler handler = partAttachment.getAttachmentData();
                Attachment attachment = new Attachment();
                attachment.setEntry(plasmid);
                attachment.setDescription(partAttachment.getDescription());
                attachment.setFileName(partAttachment.getName());
                AttachmentController attachmentController = ControllerFactory.getAttachmentController();
                try {
                    attachmentController.save(account, attachment, handler.getInputStream());
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
        }

        // check sequence
        if (plasmidTransfer.getSequence() != null) {
            String sequenceString;
            try {
                sequenceString = IOUtils.toString(plasmidTransfer.getSequence().getAttachmentData().getInputStream());
                IDNASequence dnaSequence = SequenceController.parse(sequenceString);
                if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
                    Logger.error("Couldn't parse sequence file!");
                } else {
                    Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
                    sequence.setSequenceUser(sequenceString);
                    sequence.setEntry(plasmid);
                    sequenceController.saveSequence(sequence);
                }
            } catch (IOException e) {
                Logger.error(e);
            }
        }

        return plasmid.getRecordId();
    }

    public boolean recordParts(ArrayList<PartTransfer> parts) {
        if (parts == null)
            return false;

        try {
            for (PartTransfer part : parts) {
                Entry entry = InfoToModelFactory.infoToEntry(part.getPart());
                entry.setVisibility(Visibility.TRANSFERRED.getValue());
                entry.setPartNumber(getNextPartNumber());
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
                if (part.getSequence() != null) {
                    String sequenceString;
                    try {
                        sequenceString = IOUtils.toString(part.getSequence().getAttachmentData().getInputStream());
                        IDNASequence dnaSequence = SequenceController.parse(sequenceString);
                        if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
                            Logger.error("Couldn't parse sequence file!");
                        } else {
                            Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
                            sequence.setSequenceUser(sequenceString);
                            sequence.setEntry(entry);
                            sequenceController.saveSequence(sequence);
                        }
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
            return false;
        }

        return true;
    }

    /**
     * Retrieve {@link Entry} from the database by id.
     *
     * @param account account of user performing action
     * @param id      unique local identifier for entry
     * @return entry retrieved from the database.
     * @throws ControllerException
     */
    public Entry get(Account account, long id) throws ControllerException {
        Entry entry;

        try {
            entry = dao.get(id);
            if (entry != null && !permissionsController.hasReadPermission(account, entry)) {
                throw new ControllerException(account.getEmail() + ": No read permission for entry " + id);
            }

            if (entry == null)
                return null;

            // get reverse for linked entries
            entry.getLinkedEntries().addAll(dao.getReverseLinkedEntries(id));
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return entry;
    }

    public void setStrainPlasmids(Account account, Strain strain, String plasmids) {
        strain.getLinkedEntries().clear();
        if (plasmids != null && !plasmids.isEmpty()) {
            for (String plasmid : plasmids.split(",")) {
                try {
                    Entry linked = dao.getByPartNumber(plasmid.trim());
                    if (linked == null)
                        continue;

                    if (!permissionsController.hasReadPermission(account, linked)) {
                        continue;
                    }

                    strain.getLinkedEntries().add(linked);
                } catch (DAOException | ControllerException e) {
                    Logger.error(e);
                }
            }

            try {
                update(account, strain);
            } catch (PermissionException | ControllerException pe) {
                Logger.error(pe);
            }
        }
    }

    /**
     * Retrieve {@link Entry} from the database by recordId (uuid).
     *
     * @param recordId universally unique identifier that was assigned to entry on create
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public Entry getByRecordId(Account account, String recordId) throws ControllerException, PermissionException {
        Entry entry;

        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        return entry;
    }

    public PartData getPartByRecordId(Account account, String recordId) throws ControllerException {
        Entry entry;

        try {
            entry = dao.getByRecordId(recordId);
            if (entry == null)
                return null;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (!permissionsController.hasReadPermission(account, entry)) {
            throw new ControllerException("No read permission for part with recordId " + recordId);
        }

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    public FeaturedDNASequence getPublicSequence(String recordId) throws ControllerException {
        Entry entry;
        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.isPubliclyVisible(entry)) {
            String errMsg = "Entry " + recordId + " is not public";
            Logger.warn(errMsg);
            throw new ControllerException(errMsg);
        }
        return sequenceController.sequenceToDNASequence(sequenceController.getByEntry(entry));
    }

    public PartData getPublicEntryByRecordId(String recordId) throws ControllerException {
        Entry entry;

        try {
            entry = dao.getByRecordId(recordId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.isPubliclyVisible(entry)) {
            String errMsg = "Entry " + recordId + " is not public";
            Logger.warn(errMsg);
            throw new ControllerException(errMsg);
        }

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    public PartData getPublicEntryById(long id) throws ControllerException {
        Entry entry;

        try {
            entry = dao.get(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.isPubliclyVisible(entry)) {
            String errMsg = "Entry " + id + " is not public";
            Logger.warn(errMsg);
            throw new ControllerException(errMsg);
        }

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        info.setOwnerId(0);
        info.setCreatorId(0);

        if (hasSequence) {
            String script = PigeonSBOLv.generatePigeonScript(sequenceController.getByEntry(entry));
            info.setSbolVisualURL(script);
        }

        return info;
    }

    /**
     * Retrieve {@link Entry} from the database by part number.
     * <p/>
     * Throws exception if multiple entries have the same part number.
     *
     * @param partNumber entry part number
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public PartData getByPartNumber(Account account, String partNumber) throws ControllerException,
            PermissionException {
        Entry entry;
        try {
            entry = dao.getByPartNumber(partNumber);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    /**
     * Retrieve {@link Entry} from the database by name.
     * <p/>
     * Throws exception if multiple entries have the same name.
     *
     * @param name entry name
     * @return entry retrieved from the database.
     * @throws ControllerException
     * @throws PermissionException
     */
    public PartData getByUniqueName(Account account, String name) throws ControllerException, PermissionException {
        Entry entry;
        try {
            entry = dao.getByUniqueName(name);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (entry == null)
            return null;

        if (!permissionsController.hasReadPermission(account, entry)) {
            throw new PermissionException("No read permission for entry!");
        }

        PartData info = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        info.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        info.setHasOriginalSequence(hasOriginalSequence);
        return info;
    }

    public FolderDetails retrieveVisibleEntries(Account account, ColumnField field, boolean asc, int start, int limit)
            throws ControllerException {
        Set<Entry> results;
        FolderDetails details = new FolderDetails();
        try {
            if (accountController.isAdministrator(account)) {
                // no filters
                results = dao.retrieveAllEntries(field, asc, start, limit);
            } else {
                // retrieve groups for account and filter by permission
                Set<Group> accountGroups = new HashSet<>(account.getGroups());
                GroupController controller = ControllerFactory.getGroupController();
                Group everybodyGroup = controller.createOrRetrievePublicGroup();
                accountGroups.add(everybodyGroup);
                results = dao.retrieveVisibleEntries(account, accountGroups, field, asc, start, limit);
            }

            for (Entry entry : results) {
                PartData info = ModelToInfoFactory.createTableViewData(entry, false);
                info.setCanEdit(ControllerFactory.getPermissionController().hasWritePermission(account, entry));
                details.getEntries().add(info);
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        return details;
    }

    /**
     * Retrieve the number of entries that is visible to a particular user
     *
     * @param account user account
     * @return Number of entries that user with account referenced in the parameter can read.
     * @throws ControllerException
     */
    public long getNumberOfVisibleEntries(Account account) throws ControllerException {
        if (accountController.isAdministrator(account)) {
            try {
                return dao.getAllEntryCount();
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        }

        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = ControllerFactory.getGroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);
        try {
            return dao.visibleEntryCount(account, accountGroups);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public List<Entry> retrieveOwnerEntries(Account account, String ownerEmail,
            ColumnField sort, boolean asc, int start, int limit) throws ControllerException {
        try {
            if (accountController.isAdministrator(account) || account.getEmail().equals(ownerEmail)) {
                return dao.retrieveOwnerEntries(ownerEmail, sort, asc, start, limit);
            }

            Set<Group> accountGroups = new HashSet<>(account.getGroups());
            GroupController controller = ControllerFactory.getGroupController();
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup);
            return dao.retrieveUserEntries(account, ownerEmail, accountGroups, sort, asc, start, limit);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public long getNumberOfOwnerEntries(Account account, String ownerEmail) throws ControllerException {
        try {
            if (accountController.isAdministrator(account) || account.getEmail().equals(ownerEmail)) {
                return dao.ownerEntryCount(ownerEmail);
            }

            Set<Group> accountGroups = new HashSet<>(account.getGroups());
            GroupController controller = ControllerFactory.getGroupController();
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup);
            return dao.ownerEntryCount(account, ownerEmail, accountGroups);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public long updatePart(Account account, PartData part) throws ControllerException {
        Entry existing = get(account, part.getId());
        if (!permissionsController.hasWritePermission(account, existing))
            throw new ControllerException(account.getEmail() + ": no permission to update " + part.getPartId());

        Entry entry = InfoToModelFactory.infoToEntry(part, existing);
        try {
            entry.getLinkedEntries().clear();
            if (part.getLinkedParts() != null && part.getLinkedParts().size() > 0) {
                for (PartData data : part.getLinkedParts()) {
                    Entry linked = dao.getByPartNumber(data.getPartId());
                    if (linked == null)
                        continue;

                    if (!permissionsController.hasReadPermission(account, linked)) {
                        continue;
                    }

                    entry.getLinkedEntries().add(linked);
                }
            }
        } catch (DAOException e) {
            Logger.error(e);
        }

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            entry.setVisibility(Visibility.OK.getValue());
            dao.update(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        return entry.getId();
    }

    public void update(Account account, Entry entry) throws ControllerException, PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to update null entry!");
        }

        if (!permissionsController.hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry!");
        }

        boolean scheduleRebuild = sequenceController.hasSequence(entry.getId());

        try {
            entry.setModificationTime(Calendar.getInstance().getTime());
            if (entry.getVisibility() == null)
                entry.setVisibility(Visibility.OK.getValue());
            dao.update(entry);

            if (scheduleRebuild) {
                ApplicationController.scheduleBlastIndexRebuildTask(true);
            }
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Delete the entry in the database. Schedule an index rebuild.
     *
     * @param entryId unique identifier for entry to be deleted
     * @throws ControllerException
     * @throws PermissionException
     */
    public void delete(Account account, long entryId) throws ControllerException, PermissionException {
        Entry entry;
        try {
            entry = dao.get(entryId);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
        boolean schedule = sequenceController.hasSequence(entry.getId());
//        if (entry.getVisibility() == Visibility.DRAFT.getValue()) // TODO
//            fullDelete(entry, schedule);
//        else
        delete(account, entry, schedule);
    }

    /**
     * Experimental. Do not use
     * Performs a full deletion of the entry, not just marking it as deleted.
     *
     * @param entry Entry to be deleted
     * @throws ControllerException
     */
    protected void fullDelete(Entry entry, boolean schedule) throws ControllerException {
        if (entry == null)
            return;

        try {
            dao.fullDelete(entry);
            if (schedule) {
                ApplicationController.scheduleBlastIndexRebuildTask(true);
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    /**
     * Delete the entry in the database. Optionally schedule an index rebuild.
     *
     * @param entry                entry to deleted
     * @param scheduleIndexRebuild True if index rebuild is scheduled.
     * @throws ControllerException
     * @throws PermissionException
     */
    private void delete(Account account, Entry entry, boolean scheduleIndexRebuild)
            throws ControllerException, PermissionException {
        if (entry == null) {
            throw new ControllerException("Failed to save null entry");
        }
        if (!permissionsController.hasWritePermission(account, entry)) {
            throw new PermissionException("No write permission for entry");
        }
        String deletionString = "This entry is deleted. It was owned by " + entry.getOwnerEmail();
        entry.setLongDescription(deletionString + entry.getLongDescription());
        Account sysAccount = accountController.getSystemAccount();
        entry.setOwnerEmail(sysAccount.getEmail());

        permissionsController.clearEntryPermissions(sysAccount, entry);
        entry.setModificationTime(Calendar.getInstance().getTime());

        try {
            dao.update(entry);
        } catch (DAOException e1) {
            throw new ControllerException("Failed to save entry deletion", e1);
        }

        if (scheduleIndexRebuild) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }
    }

    /**
     * Filter {@link Entry} id's for display.
     * <p/>
     * Given a List of entry id's, keep only id's that user has read access to.
     *
     * @param account user account
     * @param ids     list of entry ids
     * @return List of Entry ids.
     * @throws ControllerException
     */
    List<Long> filterEntriesByPermission(Account account, List<Long> ids) throws ControllerException {
        ArrayList<Long> result = new ArrayList<>();
        for (Long id : ids) {
            Entry entry;
            try {
                entry = dao.get(id);
            } catch (DAOException e) {
                Logger.error(e);
                continue;
            }

            if (permissionsController.hasReadPermission(account, entry)) {
                result.add(id);
            }
        }
        return result;
    }

    public ArrayList<Entry> getEntriesByIdSet(Account account, ArrayList<Long> queryResultIds)
            throws ControllerException {
        List<Long> filtered = this.filterEntriesByPermission(account, queryResultIds);
        try {
            return new ArrayList<>(dao.getEntriesByIdSet(filtered));
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public UserComment addCommentToEntry(Account account, UserComment userComment) throws ControllerException {
        Entry entry = get(account, userComment.getEntryId());
        Comment comment = new Comment(entry, account, userComment.getMessage());
        try {
            comment = commentDAO.save(comment);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        return Comment.toDTO(comment);
    }

    public PartData retrieveEntryTipDetailsFromURL(long entryId, IRegistryAPI api) throws ControllerException {
        try {
            PartData info = api.getPublicPart(entryId);
            boolean hasSequence = api.hasSequence(info.getRecordId());
            info.setHasSequence(hasSequence);
            boolean hasOriginalSequence = api.hasUploadedSequence(info.getRecordId());
            info.setHasOriginalSequence(hasOriginalSequence);
            return info;
        } catch (ServiceException se) {
            Logger.error(se);
            throw new ControllerException(se);
        }
    }

    public PartData retrieveEntryTipDetails(Account account, long entryId) throws ControllerException {
        Entry entry;

        try {
            entry = dao.get(entryId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (!permissionsController.hasReadPermission(account, entry))
            return null;

        return ModelToInfoFactory.createTipView(entry);
    }

    public PartData retrieveEntryDetailsFromURL(long entryId, IRegistryAPI api) throws ControllerException {
        try {
            PartData info = api.getPublicPart(entryId);
            boolean hasSequence = api.hasSequence(info.getRecordId());
            info.setHasSequence(hasSequence);
            boolean hasOriginalSequence = api.hasUploadedSequence(info.getRecordId());
            info.setHasOriginalSequence(hasOriginalSequence);

            if (hasSequence && info.getSbolVisualURL() != null) {
                // retrieve cached pigeon image or generate and cache
                String tmpDir = ControllerFactory.getConfigurationController()
                                                 .getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);

                String hash = Utils.generateUUID();
                URI uri = PigeonSBOLv.postToPigeon(info.getSbolVisualURL());
                if (uri != null) {
                    try {
                        IOUtils.copy(uri.toURL().openStream(),
                                     new FileOutputStream(tmpDir + File.separatorChar + hash + ".png"));
                        info.setSbolVisualURL(hash + ".png");
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            }

            return info;
        } catch (ServiceException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public void updatePartStatus(Account account, String recordId, String newStatus) throws ControllerException {
        try {
            Entry entry = getByRecordId(account, recordId);
            entry.setStatus(newStatus);
            dao.update(entry);

            if (entry.getLinkedEntries() != null) {
                for (Entry linkedEntry : entry.getLinkedEntries()) {
                    linkedEntry.setStatus(newStatus);
                    dao.update(entry);
                }
            }
        } catch (DAOException | PermissionException e) {
            throw new ControllerException(e);
        }
    }

    public PartData retrieveEntryDetails(Account account, long entryId) throws ControllerException {
        Entry entry = get(account, entryId);
        if (entry == null)
            return null;

        PartData partData = ModelToInfoFactory.getInfo(entry);
        boolean hasSequence = sequenceController.hasSequence(entry.getId());
        partData.setHasSequence(hasSequence);
        boolean hasOriginalSequence = sequenceController.hasOriginalSequence(entry.getId());
        partData.setHasOriginalSequence(hasOriginalSequence);

        // attachments
        try {
            ArrayList<Attachment> attachments = ControllerFactory.getAttachmentController().getByEntry(account, entry);
            ArrayList<AttachmentInfo> attachmentInfos = ModelToInfoFactory.getAttachments(attachments);
            partData.setAttachments(attachmentInfos);
            partData.setHasAttachment(!attachmentInfos.isEmpty());

            // samples
            ArrayList<Sample> samples = ControllerFactory.getSampleController().getSamples(entry);
            ArrayList<SampleStorage> sampleStorages = new ArrayList<>();
            if (samples != null && !samples.isEmpty()) {
                for (Sample sample : samples) {
                    SampleStorage sampleStorage = new SampleStorage();

                    // convert sample to info
                    PartSample partSample = new PartSample();
                    partSample.setCreationTime(sample.getCreationTime());
                    partSample.setLabel(sample.getLabel());
                    partSample.setNotes(sample.getNotes());
                    partSample.setDepositor(sample.getDepositor());
                    sampleStorage.setPartSample(partSample);

                    // convert sample to info
                    Storage storage = sample.getStorage();

                    while (storage != null) {
                        if (storage.getStorageType() == Storage.StorageType.SCHEME) {
                            partSample.setLocationId(storage.getId() + "");
                            partSample.setLocation(storage.getName());
                            break;
                        }

                        sampleStorage.getStorageList().add(ModelToInfoFactory.getStorageInfo(storage));
                        storage = storage.getParent();
                    }
                    sampleStorages.add(sampleStorage);
                }
            }
            partData.setSampleMap(sampleStorages);
        } catch (ControllerException ce) {
            Logger.error(ce);
        }

        // sequence analysis
        try {
            List<TraceSequence> sequences = TraceSequenceDAO.getByEntry(entry);
            partData.setSequenceAnalysis(ModelToInfoFactory.getSequenceAnalysis(sequences));
        } catch (DAOException de) {
            Logger.warn(de.getMessage());
        }

        // comments
        ArrayList<Comment> comments = commentDAO.retrieveComments(entry);
        for (Comment comment : comments) {
            partData.getComments().add(Comment.toDTO(comment));
        }

        // permissions
        partData.setCanEdit(permissionsController.hasWritePermission(account, entry));

        // viewing permissions is restricted to users who have write access
        if (partData.isCanEdit()) {
            try {
                ArrayList<AccessPermission> accessPermissions =
                        permissionsController.retrieveSetEntryPermissions(account, entry);
                partData.setAccessPermissions(accessPermissions);
                partData.setPublicRead(permissionsController.isPubliclyVisible(entry));
            } catch (PermissionException e) {
                Logger.error(e);
            }
        }

        // retrieve cached pigeon image or generate and cache
        String tmpDir = ControllerFactory.getConfigurationController()
                                         .getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        if (hasSequence) {
            Sequence sequence = sequenceController.getByEntry(entry);
            String hash = sequence.getFwdHash();
            if (Paths.get(tmpDir, hash + ".png").toFile().exists()) {
                partData.setSbolVisualURL(hash + ".png");
            } else {
                URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
                if (uri != null) {
                    try {
                        IOUtils.copy(uri.toURL().openStream(),
                                     new FileOutputStream(tmpDir + File.separatorChar + hash + ".png"));
                        partData.setSbolVisualURL(hash + ".png");
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            }
        }

        return partData;
    }

    public boolean requestSample(Account account, long entryID, String form) {
        try {
            Entry entry = dao.get(entryID);
            String email = ControllerFactory.getConfigurationController().
                    getPropertyValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (entry == null || email == null || email.isEmpty()) {
                Logger.error("Entry could not be retrieve for id " + entryID + " or bulk uploader email is not set");
                return false;
            }

            String site = ControllerFactory.getConfigurationController().getPropertyValue(ConfigurationKey.URI_PREFIX);
            StringBuilder body = new StringBuilder();
            body.append("A sample request has been received from ")
                .append(account.getFullName())
                .append(" (")
                .append("https://").append(site)
                .append("/#page=profile;id=").append(account.getId()).append(";s=profile)")
                .append(" for entry ")
                .append(entry.getPartNumber())
                .append(" (https://").append(site).append("/#page=entry;id=").append(entry.getId()).append(")")
                .append(". \n\nThe requested form is ")
                .append(form);
            return Emailer.send(email, ("Sample request for " + entry.getPartNumber()),
                                body.toString());
        } catch (DAOException | ControllerException e) {
            Logger.error(e);
            return false;
        }
    }

    public UserComment sendProblemNotification(Account account, long entryId, String msg) {
        try {
            Entry entry = dao.get(entryId);
            if (entry == null) {
                Logger.error("Entry could not be retrieve for id " + entryId);
                return null;
            }

            Comment comment = new Comment(entry, account, msg);
            comment = commentDAO.save(comment);

            String email = ControllerFactory.getConfigurationController().
                    getPropertyValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (email != null && !email.isEmpty()) {
                String site = ControllerFactory.getConfigurationController().getPropertyValue(
                        ConfigurationKey.URI_PREFIX);
                StringBuilder body = new StringBuilder();
                body.append("A problem notification was sent by ")
                    .append(account.getFullName())
                    .append(" for entry ")
                    .append(entry.getPartNumber())
                    .append(" (https://").append(site).append("/#page=entry;id=").append(entry.getId()).append(")")
                    .append("\n\nMessage:\n\n")
                    .append(msg)
                    .append("\n\n");
                boolean success = Emailer.send(email, ("Problem alert for " + entry.getPartNumber()), body.toString());
                if (!success)
                    Logger.warn("Could not send email for problem notification");
            }

            return Comment.toDTO(comment);
        } catch (DAOException | ControllerException e) {
            Logger.error(e);
        }
        return null;
    }

    public void upgradeTo3Point4() throws ControllerException {
        try {
            Logger.info("Upgrading entries. This may take several minutes...please wait");
            String prefix = ControllerFactory.getConfigurationController()
                                             .getConfiguration(ConfigurationKey.PART_NUMBER_PREFIX).getValue();
            dao.upgradeNamesAndPartNumbers(prefix);
            dao.upgradeLinks();
            Logger.info("Entry upgrade complete");
        } catch (DAOException e) {
            Logger.error(e);
        }
    }

    public void upgradeTo3Point4Point5() throws ControllerException {
        try {
            Logger.info("Upgrading funding sources. Please wait....");
            dao.upgradeFundingSources();
            Logger.info("Funding Source upgrade complete");
        } catch (DAOException e) {
            Logger.error(e);
        }

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
        if (!accountController.isAdministrator(account))
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
