package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class VerifyBulkImportPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public VerifyBulkImportPanel(String id) {
        super(id);

        BulkImportDataViewPanel panel = new BulkImportDataViewPanel("contentPanel");
        panel.setOutputMarkupId(true);
        add(panel);

        Label headerLabel = new Label("current_panel_header", "Pending Bulk Imports");
        headerLabel.setOutputMarkupId(true);

        add(headerLabel);

    }
}
