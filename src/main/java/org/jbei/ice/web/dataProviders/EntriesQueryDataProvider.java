package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class EntriesQueryDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;

    private ArrayList<String[]> queries;
    private transient EntryController entryController;

    public EntriesQueryDataProvider(ArrayList<String[]> queries) {
        super();

        this.queries = queries;

        entryController = new EntryController(IceSession.get().getAccount());
    }

    public int size() {
        int numberOfQueryEntries = 0;

        try {
            numberOfQueryEntries = entryController.getNumberOfEntriesByQueries(queries);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return numberOfQueryEntries;
    }

    public Iterator<Entry> iterator(int first, int count) {
        entries.clear();

        try {
            entries = entryController.getEntriesByQueries(queries, first, count);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return entries.iterator();
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
