package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.AuditType;
import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AuditDAO;
import org.jbei.ice.storage.model.Audit;
import org.jbei.ice.storage.model.Entry;

import java.util.Date;

public class EntryAudit {

    private final String userId;
    private final AuditDAO dao;

    public EntryAudit(String userId) {
        this.userId = userId;
        this.dao = DAOFactory.getAuditDAO();
    }

    public void fieldUpdated(long entryId, EntryField field, String value, long modificationDate) {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        if (entry == null)
            return;

        Audit audit = new Audit();
        audit.setAction(AuditType.EDIT.getAbbrev());
        audit.setUserId(this.userId);
        audit.setTime(new Date(modificationDate));
        audit.setEntryField(field);
        audit.setOldValue(value);
        audit.setEntry(entry);

        dao.create(audit);
    }

    public void action(long entryId, AuditType type, Date modificationDate) {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        if (entry == null)
            return;

        Audit audit = new Audit();
        audit.setAction(type.getAbbrev());
        audit.setUserId(this.userId);
        audit.setTime(modificationDate);
        audit.setEntry(entry);

        dao.create(audit);
    }
}
