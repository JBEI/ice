package org.jbei.ice.web.dataProviders;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.managers.FolderManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Folder;
import org.jbei.ice.web.common.ViewException;

import edu.emory.mathcs.backport.java.util.Collections;

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
        
        try {
            Folder folder = FolderManager.get(this.folder.getId());
            List<Entry> list = new LinkedList<Entry>(folder.getContents());
            
            Collections.sort(list, new Comparator<Entry>() {

                @Override
                public int compare(Entry o1, Entry o2) {

                    int result = 0;
                    if (getSort() == null)
                        return result;

                    String property = getSort().getProperty();

                    PropertyModel<Comparable<Object>> model1 = new PropertyModel<Comparable<Object>>(
                            o1, property);
                    PropertyModel<Comparable<Object>> model2 = new PropertyModel<Comparable<Object>>(
                            o2, property);

                    result = model1.getObject().compareTo(model2.getObject());

                    if (!getSort().isAscending())
                        result *= -1;

                    return result;
                }
            });
            
            List<Entry> sublist = list.subList(first, first + count);
            this.entries.addAll(sublist);
            
            return sublist.iterator();
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }
}
