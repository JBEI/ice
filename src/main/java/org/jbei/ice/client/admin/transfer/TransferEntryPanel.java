package org.jbei.ice.client.admin.transfer;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */
public class TransferEntryPanel extends Composite {

    private FlexTable layout;
    private TextArea textArea;

    public TransferEntryPanel() {
        layout = new FlexTable();
        layout.setWidth("100%");
        initWidget(layout);

        initComponents();

        layout.setHTML(0, 0, "<b class=\"font-75em\">Enter List of PartIds, Ids or Names</b>");
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);

        layout.setWidget(1, 0, textArea);
        layout.setWidget(1, 1, pendingTransferWidget());
        layout.getFlexCellFormatter().setWidth(1, 1, "300px");
    }

    private void initComponents() {
        textArea = new TextArea();
        textArea.setVisibleLines(20);
    }

    private Widget pendingTransferWidget() {
        HTML panel = new HTML(
                "<span style=\"padding: 4px; background-color: #e0eaf1; -webkit-border-radius: 5px; " +
                        "-moz-border-radius: 5px; border-radius: 5px;" +
                        "\"><b class=\"font-70em\">Pending Transfers</b></span>");
        panel.setStyleName("float_right");
        return panel;
    }
}
