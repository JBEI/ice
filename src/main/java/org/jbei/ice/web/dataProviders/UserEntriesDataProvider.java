package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class UserEntriesDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;
    private Account account;

    private transient EntryController entryController;

    public UserEntriesDataProvider(Account account) {
        super();

        this.account = account;

        entryController = new EntryController(IceSession.get().getAccount());
    }

    @Override
    public Iterator<Entry> iterator(int first, int count) {
        entries.clear();

        try {
            ArrayList<Entry> results = (ArrayList<Entry>) entryController.getEntriesByOwner(account
                    .getEmail(), first, count);

            entries.addAll(results);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return entries.iterator();
    }

    @Override
    public int size() {
        int numberOfEntries = 0;

        try {
            numberOfEntries = entryController.getNumberOfEntriesByOwner(account.getEmail());
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return numberOfEntries;
    }
}
