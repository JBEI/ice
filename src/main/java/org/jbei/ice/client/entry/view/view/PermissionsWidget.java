package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class PermissionsWidget extends Composite {

    private ListBox accountList;
    private FlexTable layout;
    private PermissionListBox readBox;
    private PermissionListBox writeBox;
    private Button saveButton;
    private Button resetButton;

    public PermissionsWidget() { // TODO : need list of account info

        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        accountList = new ListBox(true);
        accountList.setSize("300px", "325px");

        for (int i = 0; i < 50; i += 1)
            accountList.addItem("Text " + i, "Text " + i);

        layout.setWidget(0, 0, accountList);
        layout.getFlexCellFormatter().setRowSpan(0, 0, 2);
        layout.setWidget(0, 1, createReadWidget());
        layout.setWidget(1, 0, createWriteWidget());

        HorizontalPanel panel = new HorizontalPanel();
        saveButton = new Button("Save");
        resetButton = new Button("Reset");

        panel.add(saveButton);
        panel.add(resetButton);
        layout.setWidget(2, 1, panel);
        layout.getFlexCellFormatter().setRowSpan(2, 1, 2);
        layout.getFlexCellFormatter().setHorizontalAlignment(2, 1, HasAlignment.ALIGN_RIGHT);

        initWidget(layout);
    }

    private Widget createReadWidget() {
        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);

        readBox = new PermissionListBox();
        readBox.setSize("300px", "150px");

        Button addRead = new Button("Add >>");
        addRead.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int i = accountList.getSelectedIndex();
                if (i == -1)
                    return;

                for (; i < accountList.getItemCount(); i += 1) {
                    if (accountList.isItemSelected(i)) {
                        readBox.addItem(accountList.getItemText(i), accountList.getValue(i));
                    }
                }
            }
        });

        Button removeRead = new Button("Remove");
        removeRead.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                readBox.removeSelected();
            }
        });

        HTMLPanel panel = new HTMLPanel(
                "<div style=\"padding: 10px;\"><span id=\"addRead\"></span><br /><span id=\"removeRead\"></span></div>");

        panel.add(removeRead, "removeRead");
        panel.add(addRead, "addRead");

        table.setWidget(0, 0, panel);
        table.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_MIDDLE);
        table.getFlexCellFormatter().setHeight(0, 0, "150px");

        table.setWidget(0, 1, readBox);
        table.getFlexCellFormatter().setRowSpan(0, 1, 2);

        CaptionPanel captionPanel = new CaptionPanel("Read Allowed");
        captionPanel.add(table);

        return captionPanel;
    }

    private Widget createWriteWidget() {

        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);

        Button removeWrite = new Button("Remove");
        removeWrite.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                writeBox.removeSelected();
            }
        });

        Button addWrite = new Button("Add >>");
        addWrite.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int i = accountList.getSelectedIndex();
                if (i == -1)
                    return;

                for (; i < accountList.getItemCount(); i += 1) {
                    if (accountList.isItemSelected(i)) {
                        readBox.addItem(accountList.getItemText(i), accountList.getValue(i));
                        writeBox.addItem(accountList.getItemText(i), accountList.getValue(i));
                    }
                }
            }
        });

        writeBox = new PermissionListBox();
        writeBox.setSize("300px", "150px");

        HTMLPanel panel = new HTMLPanel(
                "<div style=\"padding: 10px;\"><span id=\"addWrite\"></span><br /><span id=\"removeWrite\"></span></div>");

        panel.add(addWrite, "addWrite");
        panel.add(removeWrite, "removeWrite");

        table.setWidget(0, 0, panel);
        table.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_MIDDLE);
        table.getFlexCellFormatter().setHeight(0, 0, "150px");

        table.setWidget(0, 1, writeBox);
        table.getFlexCellFormatter().setRowSpan(0, 1, 2);

        CaptionPanel captionPanel = new CaptionPanel("Write Allowed");
        captionPanel.add(table);

        return captionPanel;
    }

    private class PermissionListBox extends ListBox {

        private final HashSet<String> data;

        public PermissionListBox() {

            super(true);
            data = new HashSet<String>();
        }

        @Override
        public void addItem(String item, String value) {
            if (data.contains(value))
                return;

            data.add(value);
            super.addItem(item, value);
        }

        public void removeSelected() {
            int i = this.getSelectedIndex();
            if (i == -1)
                return;

            for (; i < this.getItemCount(); i += 1) {
                String value = this.getValue(i);
                data.remove(value);
                this.removeItem(i);
            }
        }

        public ArrayList<String> getData() {
            return new ArrayList<String>(data);
        }

    }
}
