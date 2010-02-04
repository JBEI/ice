package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Entry;

public abstract class AbstractEntriesDataProvider extends SortableDataProvider<Entry> {
    private static final long serialVersionUID = 1L;

    private HashMap<String, String> sortableFieldsMap = new HashMap<String, String>();

    protected ArrayList<Entry> entries = new ArrayList<Entry>();

    public AbstractEntriesDataProvider() {
        super();

        sortableFieldsMap.put("id", "id");
        sortableFieldsMap.put("type", "recordType");
        sortableFieldsMap.put("created", "creationTime");
        sortableFieldsMap.put("status", "status");
        sortableFieldsMap.put("summary", "short_description");

        setSort("id", true);
    }

    protected String getSortableField(String key) {
        String result = "";

        result = sortableFieldsMap.get(key);

        if (result == null) {
            result = "id";
        }

        return result;
    }

    public abstract int size();

    public IModel<Entry> model(Entry object) {
        return new Model<Entry>(object);
    }

    public abstract Iterator<Entry> iterator(int first, int count);

    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
