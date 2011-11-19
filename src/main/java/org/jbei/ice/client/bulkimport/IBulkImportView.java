package org.jbei.ice.client.bulkimport;

import org.jbei.ice.client.bulkimport.model.BulkImportMenu;

import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public interface IBulkImportView {

    Widget asWidget();

    CellList<ImportType> getMenu();

    void setHeader(String header);

    void setSheet(Widget sheet);

    Button getSaveDraftButton();

    BulkImportMenu getDraftMenu();

}
