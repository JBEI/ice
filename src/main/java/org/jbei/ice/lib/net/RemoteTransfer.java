package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.RemotePartnerDAO;
import org.jbei.ice.lib.dao.hibernate.SequenceDAO;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.services.rest.RestClient;
import org.jbei.ice.servlet.ModelToInfoFactory;

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
    public List<PartData> getPartsForTransfer(ArrayList<Long> entryIds) {
        HashSet<Long> transferred = new HashSet<>();
        HashSet<Long> toTransfer = new HashSet<>(entryIds); // for searching entries to be transferred
        List<PartData> result = new LinkedList<>();

        for (long entryId : entryIds) {
            if (transferred.contains(entryId))
                continue;

            Entry entry = entryDAO.get(entryId);
            if (entry == null)
                continue;

            // put to "/parts/
            PartData data = ModelToInfoFactory.getInfo(entry);

            // check if the linked entries (if any) is in the list of entries to be transferred
            if (data.getLinkedParts() != null) {
                Iterator<PartData> iterator = data.getLinkedParts().iterator();
                while (iterator.hasNext()) {
                    PartData linkedData = iterator.next();
                    if (!toTransfer.contains(linkedData.getId())) {
                        iterator.remove();
                        continue;
                    }
                    transferred.add(linkedData.getId());
                }
            }

            result.add(data);
        }
        return result;
    }

    /**
     * Performs the transfer of the entry objects to the remote partner specified
     *
     * @param remoteId unique identifier for remote partner the parts are to be transferred to
     * @param entries  list of entries to be transferred
     */
    public void transferEntries(long remoteId, List<PartData> entries) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return;

        RestClient client = RestClient.getInstance();
        int exceptionCount = 0;
        String url = partner.getUrl();

        for (PartData data : entries) {
            try {
                Object object = client.put(url, "/rest/parts/transfer", data, PartData.class);
                if (object == null) {
                    exceptionCount += 1;
                    continue;
                }

                data = (PartData) object;
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
        RestClient client = RestClient.getInstance();

        // check main entry for sequence
        if (sequenceDAO.hasSequence(data.getId())) {
            Entry entry = entryDAO.get(data.getId());
            Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);
            client.postSequenceFile(url, data.getRecordId(), data.getType(), sequence.getSequenceUser());
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
