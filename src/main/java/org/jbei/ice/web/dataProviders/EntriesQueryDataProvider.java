package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class EntriesQueryDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;

    private final ArrayList<String[]> queries;

    public EntriesQueryDataProvider(ArrayList<String[]> queries) {
        super();

        this.queries = queries;
    }

    @Override
    public int size() {
        long numberOfQueryEntries = 0;

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            numberOfQueryEntries = entryController.getNumberOfEntriesByQueries(queries);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return Utils.safeLongToInt(numberOfQueryEntries);
    }

    @Override
    public Iterator<Entry> iterator(int first, int count) {
        entries.clear();

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            entries = entryController.getEntriesByQueries(queries, first, count);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return entries.iterator();
    }

    @Override
    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
