package org.jbei.ice.client.entry.view.detail;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

public class UploadSequencePanel extends Composite {

    private final FlexTable layout;

    public UploadSequencePanel() {
        layout = new FlexTable();
        layout.setWidth("100%");

        layout.setHTML(0, 0,
            "<span class=\"font-80em\">Please provide either a <b>File</b> or paste <b>Sequence</b></span>");
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);

        layout.setHTML(1, 0, "<b class=\"font-80em color_444\">File</b>");
    }
}
