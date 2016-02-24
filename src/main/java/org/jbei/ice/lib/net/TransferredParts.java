package org.jbei.ice.lib.net;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.servlet.InfoToModelFactory;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Entry;

/**
 * This class represents one or more entries that have been transferred from another
 * registry
 *
 * @author Hector Plahar
 */
public class TransferredParts {
    private final EntryDAO dao;

    public TransferredParts() {
        this.dao = DAOFactory.getEntryDAO();
    }

    public PartData receiveTransferredEntry(PartData part) {
        saveTransferred(part);
        return part;
    }

    private Entry saveTransferred(PartData part) {
        Entry entry = dao.getByRecordId(part.getRecordId());
        if (entry != null) {
            Logger.info("Transferred entry found locally " + part.getRecordId());
            part.setId(entry.getId());
        } else {
            entry = InfoToModelFactory.infoToEntry(part);
            entry.setVisibility(Visibility.TRANSFERRED.getValue());
            entry = dao.create(entry);
            part.setId(entry.getId());
            part.setRecordId(entry.getRecordId());
        }

        // transfer and linked
        for (PartData data : part.getLinkedParts()) {
            Entry linked = saveTransferred(data);
            data.setId(linked.getId());
            data.setRecordId(linked.getRecordId());
            entry.getLinkedEntries().add(linked);
            dao.update(entry);
        }

        return entry;
    }
}
