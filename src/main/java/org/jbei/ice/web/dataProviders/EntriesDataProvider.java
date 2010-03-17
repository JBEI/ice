package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class EntriesDataProvider extends SortableDataProvider<Entry> {
    private static final long serialVersionUID = 1L;

    private ArrayList<Entry> entries = new ArrayList<Entry>();
    private transient EntryController entryController;

    public EntriesDataProvider() {
        super();

        entryController = new EntryController(IceSession.get().getAccount());
    }

    public Iterator<? extends Entry> iterator(int first, int count) {
        entries.clear();

        try {
            ArrayList<Entry> results = entryController.getEntries(first, count, "creationTime",
                    false);

            for (Entry entry : results) {
                entries.add(entry);
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return entries.iterator();
    }

    public IModel<Entry> model(Entry object) {
        return new Model<Entry>(object);
    }

    public int size() {
        int numberOfEntries = 0;

        try {
            numberOfEntries = entryController.getNumberOfVisibleEntries();
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return numberOfEntries;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
