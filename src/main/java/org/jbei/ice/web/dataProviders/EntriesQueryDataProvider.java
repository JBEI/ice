package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.lib.query.QueryException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

import edu.emory.mathcs.backport.java.util.Collections;

public class EntriesQueryDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;

    private final ArrayList<String[]> queries;
    private long size;
    private ArrayList<Long> entryIds = new ArrayList<Long>();

    public EntriesQueryDataProvider(ArrayList<String[]> queries) {
        super();

        this.queries = queries;
        this.size = -1;

    }

    @Override
    public int size() {
        if (this.size == -1) {
            size = 0;
            EntryController entryController = new EntryController(IceSession.get().getAccount());
            ArrayList<Long> queryResultIds;

            try {
                queryResultIds = Query.getInstance().query(queries);
                long numberOfEntriesByQueries = 0;
                for (Long entryId : queryResultIds) {
                    if (entryController.hasReadPermissionById(entryId)) {
                        entryIds.add(entryId);
                        numberOfEntriesByQueries++;
                    }
                }
                size = numberOfEntriesByQueries;
                Collections.reverse(entryIds);
            } catch (QueryException e1) {
                // Log and continue. 
                Logger.error(e1);
            } catch (ControllerException e) {
                Logger.error(e);
            }

        }

        return Utils.safeLongToInt(size);
    }

    @Override
    public Iterator<Entry> iterator(int first, int count) {
        size();
        entries.clear();

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            int endIndex = first + count;
            if (endIndex > entryIds.size()) {
                endIndex = entryIds.size();
            }
            entries = entryController.getEntriesByIdSet(entryIds.subList(first, endIndex));
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
