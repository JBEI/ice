package org.jbei.ice.web.dataProviders;

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
        Folder folder;
        try {
            folder = FolderManager.get(this.folder.getId());
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
        return folder.getContents().size();
    }

    @Override
    public Iterator<Entry> iterator(int first, int count) {

        this.entries.clear();

        String sortParam = this.getSort().getProperty();
        boolean asc = this.getSort().isAscending();

        try {
            List<Long> list = FolderManager.getFolderContents(this.folder.getId(), asc);
            List<Entry> results = null;

            if (list.size() > 1000) {
                List<Long> allEntries = EntryManager.getEntries(sortParam, asc);
                allEntries.retainAll(list);
                allEntries = allEntries.subList(first, first + count);
                results = EntryManager.getEntriesByIdSetSort(allEntries, sortParam, asc);
                this.entries.addAll(results);

                return results.iterator();

            } else {

                results = EntryManager.getEntriesByIdSetSort(list, sortParam, asc);
                List<Entry> sublist = results.subList(first, first + count);
                this.entries.addAll(sublist);

                return sublist.iterator();
            }

        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }
}
