package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;

import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public interface IBulkImportView {

    void setHeader(String header);

    void setSheet(NewBulkInput bulkImput);

    Widget asWidget();

    void showFeedback(String msg, boolean isError);

    void setSavedDraftsData(ArrayList<MenuItem> data);

    SingleSelectionModel<MenuItem> getDraftMenuModel();

    SingleSelectionModel<EntryAddType> getImportCreateModel();
}
