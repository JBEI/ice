package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Entry;

public abstract class AbstractEntriesDataProvider extends SortableDataProvider<Entry> {
    private static final long serialVersionUID = 1L;

    protected ArrayList<Entry> entries = new ArrayList<Entry>();

    public AbstractEntriesDataProvider() {
        super();
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
