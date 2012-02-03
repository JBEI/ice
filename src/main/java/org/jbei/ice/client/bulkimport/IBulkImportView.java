package org.jbei.ice.client.bulkimport;

import com.google.gwt.user.client.ui.Widget;

public interface IBulkImportView {

    BulkImportMenu getMenu();

    void setHeader(String header);

    void setSheet(Widget sheet);

    SavedDraftsMenu getDraftMenu();

    void setFeedbackPanel(Widget widget);

    Widget asWidget();
}
