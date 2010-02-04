package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.lib.query.SortField;

public class EntriesQueryDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;

    private ArrayList<String[]> queries;

    public EntriesQueryDataProvider(ArrayList<String[]> queries) {
        super();

        this.queries = queries;
    }

    public int size() {
        return Query.getInstance().queryCount(queries);
    }

    public Iterator<Entry> iterator(int first, int count) {
        entries.clear();

        try {
            SortParam sp = getSort();

            String field = getSortableField(sp.getProperty());

            LinkedHashSet<Entry> results = (LinkedHashSet<Entry>) Query.getInstance().query(
                    queries, first, count,
                    new SortField[] { new SortField(field, sp.isAscending()) });

            for (Entry entry : results) {
                entries.add(entry);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return entries.iterator();
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
