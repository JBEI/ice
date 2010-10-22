package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class EntriesDataProvider extends AbstractEntriesDataProvider {
    private static final long serialVersionUID = 1L;

    private final ArrayList<Entry> entries = new ArrayList<Entry>();

    public EntriesDataProvider() {
        super();
        
        // default sort
        setSort("creation_time", false);
    }

    @Override
    public Iterator<Entry> iterator(int first, int count) {
        entries.clear();

        EntryController entryController = new EntryController(IceSession.get().getAccount());
        
        try {
        	String sortParam = getSort().getProperty();
        	boolean asc = getSort().isAscending();
        	
            ArrayList<Entry> results = entryController.getEntries(first, count, sortParam, asc);

            for (Entry entry : results) {
                entries.add(entry);
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
        
        return entries.iterator();
    }

    @Override
    public IModel<Entry> model(Entry object) {
        return new Model<Entry>(object);
    }

    @Override
    public int size() {
        long numberOfEntries = 0;
        int result = 0;

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            numberOfEntries = entryController.getNumberOfVisibleEntries();
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        result = Utils.safeLongToInt(numberOfEntries);

        return result;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
