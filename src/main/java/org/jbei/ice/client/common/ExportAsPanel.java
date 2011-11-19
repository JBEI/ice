package org.jbei.ice.client.common;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;

public class ExportAsPanel extends Composite {

    private final HorizontalPanel panel;

    public ExportAsPanel() {

        panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        FlexTable layout = new FlexTable();
        layout.setWidget(0, 0, panel);
        layout.getFlexCellFormatter().setHorizontalAlignment(0, 0,
            HasHorizontalAlignment.ALIGN_CENTER);
        layout.setWidth("100%");

        initWidget(layout);
        init();
    }

    protected void init() {
        // Export as Text
        HTML label = new HTML("<b>Export as:</b> Printable (");
        panel.add(label);

        // current content
        Hyperlink currentContent = new Hyperlink("Current Content", "current_content");
        panel.add(currentContent);

        panel.add(new HTML("&nbsp;|&nbsp;"));

        // full content
        Hyperlink fullContent = new Hyperlink("Full Content", "full_content");
        panel.add(fullContent);

        HTML excelLabel = new HTML(") | Excel (");
        panel.add(excelLabel);

        Hyperlink currentFields = new Hyperlink("Current Fields", "current_fields");
        panel.add(currentFields);

        panel.add(new HTML("&nbsp;|&nbsp;"));

        Hyperlink allFields = new Hyperlink("All Fields", "all_fields");
        panel.add(allFields);

        panel.add(new HTML(")&nbsp|&nbsp;"));

        Hyperlink xml = new Hyperlink("XML", "xml");
        panel.add(xml);
    }

}
