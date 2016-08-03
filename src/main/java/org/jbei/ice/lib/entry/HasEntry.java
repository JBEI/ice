package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Entry;

import java.util.List;

/**
 * Parent class for all objects that have an entry, or need to retrieve one
 * Provides a means to retrieve an entry using an id that can either be
 * the database identifier for the entry object or any one of the other unique entry
 * fields. e.g. <code>part number</code> or <code>universally unique id</code>
 * <p>
 * Also provides access to the entry data accessor object
 *
 * @author Hector Plahar
 */
public class HasEntry {

    protected final EntryDAO entryDAO;
    protected final AccountDAO accountDAO;

    public HasEntry() {
        this.entryDAO = DAOFactory.getEntryDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
    }

    protected Entry getEntry(String id) {
        Entry entry = null;

        // check if numeric
        try {
            entry = entryDAO.get(Long.decode(id));
        } catch (NumberFormatException nfe) {
            // fine to ignore
        }

        // check for part Id
        if (entry == null)
            entry = entryDAO.getByPartNumber(id);

        // check for global unique id
        if (entry == null)
            entry = entryDAO.getByRecordId(id);

        // get by unique name
        if (entry == null) {
            List<Entry> result = entryDAO.getByName(id);
            if (result == null || result.isEmpty())
                return null;

            if (result.size() == 1)
                return result.get(0);

            if (result.size() > 1) {
                Logger.error("Multiple entries found with name " + id);
                return null;
            }
        }

        return entry;
    }
}
