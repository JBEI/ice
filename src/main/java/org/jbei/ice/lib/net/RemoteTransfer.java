package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.composers.formatters.GenbankFormatter;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.RemotePartner;
import org.jbei.ice.storage.model.Sequence;

import java.util.*;

/**
 * @author Hector Plahar
 */
public class RemoteTransfer {

    private final RemotePartnerDAO remotePartnerDAO;
    private final EntryDAO entryDAO;

    public RemoteTransfer() {
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.entryDAO = DAOFactory.getEntryDAO();
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
     */
    public void transferEntries(long remoteId, List<PartData> entries) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return;

        IceRestClient client = IceRestClient.getInstance();
        int exceptionCount = 0;
        String url = partner.getUrl();

        for (PartData data : entries) {
            try {
                PartData object = client.put(url, "/rest/parts/transfer", data, PartData.class);
                if (object == null) {
                    exceptionCount += 1;
                    continue;
                }

                performTransfer(partner, data);
            } catch (Exception e) {
                exceptionCount += 1;
                if (exceptionCount >= 5) {
                    Logger.error(e);
                    Logger.error(exceptionCount + " exceptions encountered during transfer. Aborting");
                    return;
                }

                Logger.error(e);
            }
        }
    }

    /**
     * Transfers the sequence files for the part and any parts that are linked to it
     *
     * @param partner destination for the sequence transfer
     * @param data    data for part whose sequences are to be transferred
     */
    protected void performTransfer(RemotePartner partner, PartData data) {
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
        String url = partner.getUrl();
        IceRestClient client = IceRestClient.getInstance();

        // check main entry for sequence
        if (sequenceDAO.hasSequence(data.getId())) {
            Entry entry = entryDAO.get(data.getId());
            Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);
            SequenceController controller = new SequenceController();
            GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getName());
            genbankFormatter.setCircular(true);
            String sequenceString;
            try {
                sequenceString = controller.compose(sequence, genbankFormatter);
            } catch (Exception e) {
                Logger.error(e);
                sequenceString = sequence.getSequenceUser();
            }
            if (StringUtils.isEmpty(sequenceString))
                sequenceString = sequence.getSequence();

            client.postSequenceFile(url, data.getRecordId(), data.getType(), sequenceString);
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
