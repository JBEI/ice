package org.jbei.ice.lib.net;

import org.apache.commons.lang3.StringUtils;
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
        // check the record id
        if (StringUtils.isNotEmpty(part.getRecordId())) {
            Entry entry = dao.getByRecordId(part.getRecordId());
            if (entry != null) {
                Logger.warn("Transferred entry's record id \"" + part.getRecordId() + "\" conflicts with existing");
                return null;
            }
        }

        Entry entry = saveTransferred(part);
        if (entry == null)
            return null;
        part.setId(entry.getId());
        part.setRecordId(entry.getRecordId());
        return part;
    }

    private Entry saveTransferred(PartData part) {
        Entry entry = InfoToModelFactory.infoToEntry(part);
        if (entry == null) {
            return null;
        }

        entry.setVisibility(Visibility.TRANSFERRED.getValue());
        entry = dao.create(entry);

        // transfer and linked
        if (part.getLinkedParts() != null) {
            for (PartData data : part.getLinkedParts()) {
                // check if linked already exists before creating
                Entry linked = dao.getByRecordId(data.getRecordId());
                if (linked == null)
                    linked = saveTransferred(data);
                entry.getLinkedEntries().add(linked);
                dao.update(entry);
            }
        }

        return entry;
    }
}
