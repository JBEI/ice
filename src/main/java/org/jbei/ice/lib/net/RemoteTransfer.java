package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.EntrySelectionType;
import org.jbei.ice.lib.entry.sequence.InputStreamWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceAsString;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.RemotePartner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Hector Plahar
 */
public class RemoteTransfer {

    private final RemotePartnerDAO remotePartnerDAO;
    private final EntryDAO entryDAO;
    private final RemoteContact remoteContact;
    private final SequenceDAO sequenceDAO;

    public RemoteTransfer() {
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.remoteContact = new RemoteContact();
        this.entryDAO = DAOFactory.getEntryDAO();
        this.sequenceDAO = DAOFactory.getSequenceDAO();
    }

    /**
     * Using the list of entry Ids, populates a list of PartData objects that maintains the hierarchical
     * relationships
     *
     * @param entryIds list of ids for entries that are to be transferred
     * @return List of Part Data objects obtained using the list for transfer
     */
    public List<PartData> getPartsForTransfer(List<Long> entryIds) {
        HashSet<Long> forTransfer = new HashSet<>(entryIds); // for searching entries to be transferred
        HashMap<Long, PartData> toTransfer = new LinkedHashMap<>();

        for (long entryId : entryIds) {
            // already being transferred; skip
            if (toTransfer.containsKey(entryId))
                continue;

            Entry entry = entryDAO.get(entryId);
            if (entry == null)
                continue;

            PartData data = ModelToInfoFactory.getInfo(entry);
            if (data == null) {
                Logger.error("Could not convert entry " + entry.getId() + " to data model");
                continue;
            }

            // check if the linked entries (if any) is in the list of entries to be transferred
            if (data.getLinkedParts() != null && !data.getLinkedParts().isEmpty()) {
                Iterator<PartData> iterator = data.getLinkedParts().iterator();
                while (iterator.hasNext()) {
                    PartData linkedData = iterator.next();

                    // make sure linked entry is among list to be transferred
                    if (!forTransfer.contains(linkedData.getId())) {
                        iterator.remove();
                        continue;
                    }

                    // check if linked entry has already been transferred
                    if (toTransfer.containsKey(linkedData.getId())) {
                        // then remove from list of entries to be transferred as it will be transferred as
                        // part of this entry
                        if (toTransfer.remove(linkedData.getId()) == null)
                            Logger.warn("Entry " + linkedData.getId() + " being transferred twice");
                    }
                }
            }

            toTransfer.put(data.getId(), data);
        }
        return new LinkedList<>(toTransfer.values());
    }

    /**
     * Performs the transfer of the entry objects to the remote partner specified.
     * It is the responsibility of the destination to ensure that the hierarchical reln is reconstructed
     *
     * @param remoteId unique identifier for remote partner the parts are to be transferred to
     * @param entries  list of entries to be transferred. Note that the entries contain the linked
     *                 entries as well and these may or may not already exist on the recipient
     * @return list of ids of the transferred entries. These are the ids on the remote recipient and not this ice instance
     */
    public List<Long> transferEntries(long remoteId, List<PartData> entries) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            throw new IllegalArgumentException("Invalid remote host id: " + remoteId);

        int exceptionCount = 0;
        String url = partner.getUrl();
        List<Long> remoteIds = new LinkedList<>();

        for (PartData data : entries) {
            try {
                // fetch linked parts to enable remote to create the links
                if (data.getLinkedParts() != null && !data.getLinkedParts().isEmpty()) {
                    List<PartData> linkedParts = new ArrayList<>();
                    for (PartData linkedData : data.getLinkedParts()) {
                        Entry entry = entryDAO.get(linkedData.getId());
                        if (entry == null)
                            continue;

                        linkedData = ModelToInfoFactory.getInfo(entry);
                        linkedParts.add(linkedData);
                    }
                    data.getLinkedParts().clear();
                    data.getLinkedParts().addAll(linkedParts);
                }

                // transfer the part with information about links (if any)
                PartData object = remoteContact.transferPart(url, data);
                if (object == null) {
                    exceptionCount += 1;
                    continue;
                }

                remoteIds.add(object.getId());
                if (data.getLinkedParts() != null) {
                    remoteIds.addAll(object.getLinkedParts().stream().map(PartData::getId).collect(Collectors.toList()));
                }

                // transfers attachments and sequences
                performTransfer(partner, data);
            } catch (Exception e) {
                exceptionCount += 1;
                if (exceptionCount >= 5) {
                    Logger.error(e);
                    Logger.error(exceptionCount + " exceptions encountered during transfer. Aborting");
                    return null;
                }

                Logger.error(e);
            }
        }

        return remoteIds;
    }

    public FolderDetails transferFolder(long remoteId, FolderDetails folderDetails, List<Long> remoteIds) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            throw new IllegalArgumentException("Invalid remote host id: " + remoteId);

        FolderDetails details = remoteContact.transferFolder(partner.getUrl(), folderDetails);
        if (folderDetails == null) {
            Logger.error("Could not create remote folder");
            return null;
        }

        if (remoteIds == null || remoteIds.isEmpty()) {
            Logger.info("Skipping transfer of entries. List is empty");
            return details;
        }

        // move entries to the transferred entries
        EntrySelection entrySelection = new EntrySelection();
        entrySelection.getEntries().addAll(remoteIds);
        entrySelection.getDestination().add(details);
        entrySelection.setSelectionType(EntrySelectionType.FOLDER);

        remoteContact.addTransferredEntriesToFolder(partner.getUrl(), entrySelection);
        return details;
    }

    /**
     * Transfers the sequence file for the part and any parts that are linked to it.
     * If the attached sequence was uploaded as a file or pasted, the system
     * transfers that. If not if attempts to convert the attached sequence to genbank format
     * and transfers that
     *
     * @param partner destination for the sequence transfer
     * @param data    data for part whose sequences are to be transferred
     */
    protected void performTransfer(RemotePartner partner, PartData data) {
        String url = partner.getUrl();

        // check main entry for sequence
        if (sequenceDAO.hasSequence(data.getId())) {
            InputStreamWrapper wrapper = new SequenceAsString(SequenceFormat.GENBANK, data.getId(), true).get();
            if (wrapper != null && wrapper.getInputStream() != null) {
                try {
                    String sequenceString = new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    remoteContact.transferSequence(url, data.getRecordId(), data.getType(), sequenceString);
                } catch (IOException e) {
                    Logger.error("Cannot transfer sequence", e);
                }
            }
        }

        // todo : check main entry for attachments

        // add any attachment(s)
//                AttachmentController attachmentController = new AttachmentController();
//                attachmentController.getByEntry(userId, entry);
//
//                AttachmentInfo info = postAttachmentFile(sessionId, j5Result);
//                if (info == null) {
//                    Logger.error("Could not upload file to registry");
//                    return false;
//                }
//
//                info.setDescription("J5 run result");
//                post("/rest/parts/" + plasmid.getId() + "/attachments", sessionId, info, AttachmentInfo.class);


        // check child entries
        if (data.getLinkedParts() == null)
            return;

        for (PartData linked : data.getLinkedParts()) {
            performTransfer(partner, linked);
        }
    }
}
