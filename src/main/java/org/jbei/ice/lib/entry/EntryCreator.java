package org.jbei.ice.lib.entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import javax.activation.DataHandler;

import org.jbei.ice.ApplicationController;
import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.Permission;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.PermissionDAO;
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.PartSample;
import org.jbei.ice.lib.dto.StorageInfo;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.dto.sample.SampleStorage;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.SampleCreator;
import org.jbei.ice.lib.entry.sample.StorageController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.DNASequence;
import org.jbei.ice.lib.vo.PartAttachment;
import org.jbei.ice.lib.vo.PartTransfer;
import org.jbei.ice.servlet.InfoToModelFactory;

import org.apache.commons.io.IOUtils;

/**
 * @author Hector Plahar
 */
public class EntryCreator {

    private final EntryDAO dao;
    private final PermissionDAO permissionDAO;
    private final SequenceDAO sequenceDAO;

    public EntryCreator() {
        dao = DAOFactory.getEntryDAO();
        permissionDAO = DAOFactory.getPermissionDAO();
        sequenceDAO = DAOFactory.getSequenceDAO();
    }

    /**
     * creates entry and assigns read permissions to all public groups that user creating the entry is a member of
     *
     * @param account account for user creating entry
     * @param entry   entry being created
     * @return created entry
     * @throws org.jbei.ice.ControllerException
     *          on exception creating the entry
     */
    public Entry createEntry(Account account, Entry entry) throws ControllerException {
        PermissionsController permissionsController = new PermissionsController();
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
     */
    public Entry createEntry(Account account, Entry entry, ArrayList<AccessPermission> accessPermissions) {
        entry.setPartNumber(EntryUtil.getNextPartNumber());
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

        entry = dao.create(entry);

        // add write permissions for owner
        Permission permission = new Permission();
        permission.setCanWrite(true);
        permission.setEntry(entry);
        permission.setAccount(account);
        permissionDAO.create(permission);

        // add read permission for all public groups
        ArrayList<Group> groups = new GroupController().getAllPublicGroupsForAccount(account);
        for (Group group : groups) {
            Permission groupPermission = new Permission();
            groupPermission.setGroup(group);
            groupPermission.setEntry(entry);
            groupPermission.setCanRead(true);
            permissionDAO.create(groupPermission);
        }

        if (accessPermissions != null) {
            for (AccessPermission accessPermission : accessPermissions) {
                if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT) {
                    // TODO
                    // add account permission

                } else {
                    // add group permission
                }
            }
        }

        // rebuild blast database
        if (sequenceDAO.hasSequence(entry.getId())) {
            ApplicationController.scheduleBlastIndexRebuildTask(true);
        }

        return entry;
    }

    public PartData createPart(String userId, PartData part) throws ControllerException {
        Entry entry = InfoToModelFactory.infoToEntry(part);
        EntryAuthorization authorization = new EntryAuthorization();

        if (part.getLinkedParts() != null && part.getLinkedParts().size() > 0) {
            for (PartData data : part.getLinkedParts()) {
                Entry linked = dao.getByPartNumber(data.getPartId());
                if (linked == null)
                    continue;

                if (!authorization.canRead(userId, linked)) {
                    continue;
                }

                entry.getLinkedEntries().add(linked);
            }
        }
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        SampleController sampleController = new SampleController();
        StorageController storageController = new StorageController();
        ArrayList<SampleStorage> sampleMap = part.getSampleStorage();

        if (part.getInfo() != null) {
            // check if enclosed already exists
            Entry enclosed = dao.get(part.getInfo().getId());
            if (enclosed == null) {
                enclosed = InfoToModelFactory.infoToEntry(part.getInfo());
                Entry created = createStrainWithPlasmid(account, entry, enclosed, part.getAccessPermissions());
                part.setRecordId(created.getRecordId());
            } else {
                // already exists, create strain and link  TODO
//                updatePart(account, part.getInfo());
//                enclosed = get(account, part.getInfo().getId());
                entry = createEntry(account, entry, part.getAccessPermissions());
                entry.getLinkedEntries().add(enclosed);
                dao.update(entry);
            }
        } else {
            entry = createEntry(account, entry, part.getAccessPermissions());
            part.setRecordId(entry.getRecordId());
        }
        if (sampleMap != null) {
            for (SampleStorage sampleStorage : sampleMap) {
                PartSample partSample = sampleStorage.getPartSample();
                LinkedList<StorageInfo> locations = sampleStorage.getStorageList();

                Sample sample = SampleCreator.createSampleObject(partSample.getLabel(),
                                                                 account.getEmail(), partSample.getNotes());
                sample.setEntry(entry);

                if (locations == null || locations.isEmpty()) {
                    // create sample, but not location
                    Logger.info("Creating sample without location");
                    sampleController.saveSample(account, sample);
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
                    sampleController.saveSample(account, sample);
                }
            }
        }

        // save attachments
        if (part.getAttachments() != null) {
            AttachmentController attachmentController = new AttachmentController();
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
                AttachmentController attachmentController = new AttachmentController();
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
                DNASequence dnaSequence = SequenceController.parse(sequenceString);
                if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
                    Logger.error("Couldn't parse sequence file!");
                } else {
                    Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
                    sequence.setSequenceUser(sequenceString);
                    sequence.setEntry(strain);
                    sequenceDAO.saveSequence(sequence);
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
                AttachmentController attachmentController = new AttachmentController();
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
                DNASequence dnaSequence = SequenceController.parse(sequenceString);
                if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
                    Logger.error("Couldn't parse sequence file!");
                } else {
                    Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
                    sequence.setSequenceUser(sequenceString);
                    sequence.setEntry(plasmid);
                    DAOFactory.getSequenceDAO().saveSequence(sequence);
                }
            } catch (IOException e) {
                Logger.error(e);
            }
        }

        return plasmid.getRecordId();
    }

    public Entry createStrainWithPlasmid(Account account, Entry strain, Entry plasmid,
            ArrayList<AccessPermission> accessPermissions) throws ControllerException {
        if (strain == null || plasmid == null)
            throw new ControllerException("Cannot create null entries");

        plasmid = createEntry(account, plasmid, accessPermissions);
        strain = createEntry(account, strain, accessPermissions);
        strain.getLinkedEntries().add(plasmid);
        return dao.update(strain);
    }
}
