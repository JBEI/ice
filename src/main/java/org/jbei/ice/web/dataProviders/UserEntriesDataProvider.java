package org.jbei.ice.web.dataProviders;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.query.SortField;

public class UserEntriesDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;
    private Account account;

    public UserEntriesDataProvider(Account account) {
        super();

        this.account = account;
    }

    @Override
    public Iterator<Entry> iterator(int first, int count) {
        entries.clear();

        try {
            SortParam sp = getSort();

            String field = getSortableField(sp.getProperty());

            LinkedHashSet<Entry> results = AuthenticatedEntryManager.getByAccountVisible(account,
                    first, count, new SortField[] { new SortField(field, sp.isAscending()) });

            entries.addAll(results);
        } catch (Exception e) {
            Logger.warn("UserEntriesDataProvider error: " + e.toString());
        }

        return entries.iterator();
    }

    @Override
    public int size() {
        return AuthenticatedEntryManager.getByAccountVisibleCount(account);
    }
}
