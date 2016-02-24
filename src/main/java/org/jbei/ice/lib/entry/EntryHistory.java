package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.History;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.AuditDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Audit;
import org.jbei.ice.storage.model.Entry;

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
     * @throws IllegalArgumentException                    if the entry specified by id <code>entryId</code> is not located
     * @throws org.jbei.ice.lib.access.PermissionException if the specified user does not have write privileges for the
     *                                                     entry
     */
    public EntryHistory(String userId, long entryId) {
        this.dao = DAOFactory.getAuditDAO();
        this.entryDAO = DAOFactory.getEntryDAO();
        this.userId = userId;
        this.entry = entryDAO.get(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Cannot retrieve entry with id " + entryId);
        this.entryAuthorization = new EntryAuthorization();
        this.entryAuthorization.expectWrite(userId, this.entry);
    }

    public Results<History> get(int limit, int offset, boolean asc, String sort) {
        entryAuthorization.expectWrite(userId, entry);
        List<Audit> list = dao.getAuditsForEntry(entry, limit, offset, asc, sort);
        Results<History> results = new Results<>();

        AccountDAO accountDAO = DAOFactory.getAccountDAO();

        for (Audit audit : list) {
            History history = audit.toDataTransferObject();
            if (history.isLocalUser()) {
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
}
