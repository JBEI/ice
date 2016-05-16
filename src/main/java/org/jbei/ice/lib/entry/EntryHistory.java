package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.dto.AuditType;
import org.jbei.ice.lib.dto.History;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.AuditDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Audit;
import org.jbei.ice.storage.model.Entry;

import java.util.Date;
import java.util.List;

/**
 * History information for a specified entry
 *
 * @author Hector Plahar
 */
public class EntryHistory {

    private final EntryDAO entryDAO;
    private final AuditDAO dao;
    private final String userId;
    private final Entry entry;
    private final EntryAuthorization entryAuthorization;

    /**
     * @param userId  unique identifier for user making requests
     * @param entryId unique identifier for entry whose history is of interest
     * @throws IllegalArgumentException if the entry specified by id <code>entryId</code> is not located
     */
    public EntryHistory(String userId, long entryId) {
        this.dao = DAOFactory.getAuditDAO();
        this.entryDAO = DAOFactory.getEntryDAO();
        this.userId = userId;
        this.entry = entryDAO.get(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Cannot retrieve entry with id " + entryId);
        this.entryAuthorization = new EntryAuthorization();
    }

    public Results<History> get(int limit, int offset, boolean asc, String sort) {
        entryAuthorization.expectWrite(userId, entry);
        List<Audit> list = dao.getAuditsForEntry(entry, limit, offset, asc, sort);
        Results<History> results = new Results<>();

        AccountDAO accountDAO = DAOFactory.getAccountDAO();

        for (Audit audit : list) {
            History history = audit.toDataTransferObject();
            if (history.getPartner() == null) {
                Account account = accountDAO.getByEmail(history.getUserId());
                if (account != null)
                    history.setAccount(account.toDataTransferObject());
            }
            results.getData().add(history);
        }
        long count = dao.getHistoryCount(this.entry);
        results.setResultCount(count);
        return results;
    }

    public boolean delete(long historyId) {
        Audit audit = dao.get(historyId);
        if (audit == null)
            return true;

        dao.delete(audit);
        return true;
    }

    /**
     * Adds a read history object for the specified user and entry
     *
     * @return true if the object was successfully added, false otherwise
     */
    public boolean add() {
        Audit audit = new Audit();
        audit.setAction(AuditType.READ.getAbbrev());
        audit.setEntry(entry);
        audit.setUserId(userId);
        audit.setTime(new Date());
        return dao.create(audit) != null;
    }

    /**
     * Delete all available history for a specified entry
     * Due to the destructive nature, it is required that the user be
     * the owner or an administrator
     *
     * @return the number of audit objects that were deleted
     * @throws PermissionException if the user performing action is neither an admin nor owner of the entry
     */
    public int deleteAll() {
        if (!entryAuthorization.isAdmin(this.userId) &&
                !entryAuthorization.getOwner(this.entry).equalsIgnoreCase(this.userId))
            throw new PermissionException(this.userId + " cannot delete all history for entry " + this.entry.getId());
        return dao.deleteAll(this.entry);
    }
}
