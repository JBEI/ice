package org.jbei.ice.lib.net;

import java.util.ArrayList;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.RemotePartnerDAO;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.services.rest.RestClient;
import org.jbei.ice.servlet.ModelToInfoFactory;

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

    // action is restricted to admins
    public void transferEntries(long remoteId, ArrayList<Long> entries) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return;

        RestClient client = RestClient.getInstance();
        int exceptionCount = 0;
        String url = partner.getUrl();

        for (long entryId : entries) {
            Entry entry = entryDAO.get(entryId);
            if (entry == null)
                continue;

            // put to "/parts/
            PartData data = ModelToInfoFactory.getInfo(entry);
            try {
                Object object = client.put(url, "/rest/parts/transfer", data, PartData.class);
                data = (PartData) object;

                // transfer sequence (if any)
                Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);
                if (sequence != null) {
                    client.postSequenceFile(url, data.getRecordId(), data.getType(), sequence.getSequenceUser());
                }

                // todo : transfer attachments (if any)

                // todo : transfer comments??? (if any)
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
}
