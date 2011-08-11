package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.dataProviders.BulkImportDataProvider;

public class BulkImportDataViewPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public BulkImportDataViewPanel(String id) {
        super(id);

        BulkImportDataProvider dataProvider = new BulkImportDataProvider();
        BulkImportDataView view = new BulkImportDataView("bulk_import_dataview", dataProvider, null);
        add(view);
        add(new JbeiPagingNavigator("navigator", view));
    }
}
