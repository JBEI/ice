package org.jbei.ice.web.dataProviders;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.query.SortField;

public class UserEntriesDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;
    private Account account;

    public UserEntriesDataProvider(Account account) {
        super();

        this.account = account;
    }

    public Iterator<Entry> iterator(int first, int count) {
        entries.clear();

        try {
            SortParam sp = getSort();

            String field = getSortableField(sp.getProperty());

            LinkedHashSet<Entry> results = (LinkedHashSet<Entry>) EntryManager.getByAccount(
                    account, first, count,
                    new SortField[] { new SortField(field, sp.isAscending()) });

            for (Entry entry : results) {
                entries.add(entry);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return entries.iterator();
    }

    public int size() {
        return EntryManager.getByAccountCount(account);
    }
}
