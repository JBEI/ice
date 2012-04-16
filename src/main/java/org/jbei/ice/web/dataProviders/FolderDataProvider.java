package org.jbei.ice.web.dataProviders;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.FolderManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Folder;
import org.jbei.ice.web.common.ViewException;

public class FolderDataProvider extends AbstractEntriesDataProvider {

    private static final long serialVersionUID = 1L;
    private final Folder folder;

    public FolderDataProvider(Folder folder) {
        this.folder = folder;

        setSort("creationTime", false);
    }

    @Override
    public int size() {
        try {
            return FolderManager.getFolderSize(this.folder.getId());
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }

    @Override
    public Iterator<Entry> iterator(int first, int count) {

        this.entries.clear();

        String sortParam = this.getSort().getProperty();
        boolean asc = this.getSort().isAscending();

        try {
            ArrayList<BigInteger> list = FolderManager.getFolderContents(this.folder.getId(), asc);
            List<Entry> results = null;
            if ("oneName.name".equals(sortParam)) {
                List<Long> sortedEntries = EntryManager.getEntriesSortByName(asc);
                sortedEntries.retainAll(list);
                sortedEntries = sortedEntries.subList(first, first + count);
                results = EntryManager.getEntriesByIdSetSort(sortedEntries, "id", asc);

            } else if ("onePartNumber.partNumber".equals(sortParam)) {
                List<Long> sortedEntries = EntryManager.getEntriesSortByPartNumber(asc);
                sortedEntries.retainAll(list);
                sortedEntries = sortedEntries.subList(first, first + count);
                results = EntryManager.getEntriesByIdSetSort(sortedEntries, "id", asc);
            } else {

                // sort all the records and get count from main list
                List<Long> sortedEntries = EntryManager.getEntries(sortParam, asc);
                sortedEntries.retainAll(list);
                sortedEntries = sortedEntries.subList(first, first + count);
                results = EntryManager.getEntriesByIdSetSort(sortedEntries, sortParam, asc);
            }

            this.entries.addAll(results);
            return results.iterator();

        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }
}
