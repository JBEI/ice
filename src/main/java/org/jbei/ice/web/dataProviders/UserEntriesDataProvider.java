package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class UserEntriesDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;
    private final Account account;

    public UserEntriesDataProvider(Account account) {
        super();

        this.account = account;

        // default sort
        setSort("creationTime", false);
    }

    @Override
    public Iterator<Entry> iterator(int first, int count) {
        entries.clear();

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            String sort = getSort().getProperty();
            boolean asc = getSort().isAscending();
            
            ArrayList<Entry> results = entryController.getEntriesByOwner(account.getEmail(), first,
                count, sort, asc);
            if (results != null) {
                entries.addAll(results);
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
        return entries.iterator();
    }

    @Override
    public int size() {
        long numberOfEntries = 0;

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            numberOfEntries = entryController.getNumberOfEntriesByOwner(account.getEmail());
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return Utils.safeLongToInt(numberOfEntries);
    }
}
