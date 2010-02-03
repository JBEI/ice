package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.lib.query.SortField;

public class EntriesQueryDataProvider extends SortableDataProvider<Entry> {
    private static final long serialVersionUID = 1L;

    private HashMap<String, String> sortableFieldsMap = new HashMap<String, String>();

    private ArrayList<Entry> entries = new ArrayList<Entry>();

    private ArrayList<String[]> queries;

    public EntriesQueryDataProvider(ArrayList<String[]> queries) {
        super();

        sortableFieldsMap.put("id", "id");
        sortableFieldsMap.put("type", "recordType");
        sortableFieldsMap.put("created", "creationTime");
        sortableFieldsMap.put("status", "status");
        sortableFieldsMap.put("summary", "short_description");

        this.queries = queries;

        setSort("id", true);
    }

    private String getSortableField(String key) {
        String result = "";

        result = sortableFieldsMap.get(key);

        if (result == null) {
            result = "id";
        }

        return result;
    }

    public int size() {
        return Query.getInstance().queryCount(queries);
    }

    public IModel<Entry> model(Entry object) {
        return new Model<Entry>(object);
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
