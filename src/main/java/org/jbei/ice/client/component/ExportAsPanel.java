package org.jbei.ice.client.component;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class ExportAsPanel extends Composite {

    private final HorizontalPanel panel;
    private final EntryTable table;
    private Widget widget;

    public ExportAsPanel(EntryTable table) {

        panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        this.table = table;

        HorizontalPanel layout = new HorizontalPanel();
        layout.add(panel);
        layout.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        layout.setWidth("100%");

        initWidget(layout);
        init();
    }

    protected void init() {
        // Export as Text
        HTML label = new HTML("<b>Export as:</b> Printable (");
        panel.add(label);

        // current content
        //        Hyperlink currentContent = new Hyperlink("Current Content", "current_content");
        Button currentContent = createCurrentContent();
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

    private Button createCurrentContent() {

        Button button = new Button("Current Content");
        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                widget = RootPanel.get("foo").getWidget(0);
                RootPanel.get("foo").remove(0);
                RootPanel.get("foo").add(table);
            }
        });
        return button;
    }

}
