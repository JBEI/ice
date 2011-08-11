package org.jbei.ice.web.dataProviders;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.BulkImportManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.BulkImport;
import org.jbei.ice.web.common.ViewException;

public class BulkImportDataProvider extends SortableDataProvider<BulkImport> {

    private static final long serialVersionUID = 1L;

    @Override
    public Iterator<? extends BulkImport> iterator(int first, int count) {
        try {
            List<BulkImport> results = BulkImportManager.retrieveAll()
                    .subList(first, first + count);
            return results.iterator();
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }

    @Override
    public int size() {
        try {
            return BulkImportManager.retrieveAll().size();
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }

    @Override
    public IModel<BulkImport> model(BulkImport object) {
        return new Model<BulkImport>(object);
    }
}
