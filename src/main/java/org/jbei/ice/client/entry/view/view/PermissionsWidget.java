package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jbei.ice.client.entry.view.PermissionsPresenter;
import org.jbei.ice.client.entry.view.PermissionsPresenter.IPermissionsView;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class PermissionsWidget extends Composite implements IPermissionsView {

    private ListBox accountList;
    private ListBox groupList;

    private FlexTable layout;
    private PermissionListBox readBox;
    private PermissionListBox writeBox;
    private Button saveButton;
    private Button resetButton;

    private TabLayoutPanel tabPanel;
    private final PermissionsPresenter presenter;

    public PermissionsWidget() {
        readBox = new PermissionListBox();
        writeBox = new PermissionListBox();

        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        layout.setWidget(0, 0, createMainWidget());
        layout.getFlexCellFormatter().setRowSpan(0, 0, 2);
        layout.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        layout.setWidget(0, 1, createPermissionBoxWidget(readBox, "Read Allowed", false));
        layout.setWidget(1, 0, createPermissionBoxWidget(writeBox, "Write Allowed", true));

        HorizontalPanel panel = new HorizontalPanel();
        saveButton = new Button("Save");
        resetButton = new Button("Reset");

        panel.add(saveButton);
        panel.add(resetButton);
        layout.setWidget(2, 1, panel);
        layout.getFlexCellFormatter().setRowSpan(2, 1, 2);
        layout.getFlexCellFormatter().setHorizontalAlignment(2, 1, HasAlignment.ALIGN_RIGHT);

        initWidget(layout);
        presenter = new PermissionsPresenter(this);
    }

    public PermissionsPresenter getPresenter() {
        return this.presenter;
    }

    @Override
    public HandlerRegistration addUpdatePermissionsHandler(ClickHandler handler) {
        return saveButton.addClickHandler(handler);
    }

    /**
     * Creates widget that contains the account list and group list in separate tab
     * 
     * @return created widget
     */
    private Widget createMainWidget() {

        tabPanel = new TabLayoutPanel(2.2, Unit.EM);
        tabPanel.setSize("200px", "350px");
        Label groupsLabel = new Label("Groups");
        Label usersLabel = new Label("Users");

        // create accounts list
        accountList = new ListBox(true);
        accountList.setSize("200px", "350px");
        accountList.setStyleName("input_box");

        // create groups list
        groupList = new ListBox(true);
        groupList.setSize("200px", "350px");
        groupList.setStyleName("input_box");

        tabPanel.add(accountList, usersLabel);
        tabPanel.add(groupList, groupsLabel);

        return tabPanel;
    }

    private Widget createPermissionBoxWidget(final PermissionListBox box, String caption,
            boolean isWrite) {

        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);

        Button addRead = new Button("Add >>");
        addRead.addClickHandler(new AddToClickHandler(isWrite));

        Button removeRead = new Button("Remove");
        removeRead.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                box.removeSelected();
            }
        });

        HTMLPanel panel = new HTMLPanel(
                "<div style=\"padding: 10px;\"><span id=\"add\"></span><br /><span id=\"remove\"></span></div>");

        panel.add(removeRead, "remove");
        panel.add(addRead, "add");

        table.setWidget(0, 0, panel);
        table.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_MIDDLE);
        table.getFlexCellFormatter().setHeight(0, 0, "150px");

        table.setWidget(0, 1, box);
        table.getFlexCellFormatter().setRowSpan(0, 1, 2);

        CaptionPanel captionPanel = new CaptionPanel(caption);
        captionPanel.add(table);

        return captionPanel;
    }

    // public methods
    public void setExistingPermissions(ArrayList<PermissionInfo> permissions) {
        if (permissions == null)
            return;

        for (PermissionInfo info : permissions) {
            switch (info.getType()) {
            case READ_ACCOUNT:
            case READ_GROUP:
                readBox.addItem(info.getDisplay(), String.valueOf(info.getId()), info.getType());
                break;

            case WRITE_ACCOUNT:
            case WRITE_GROUP:
                writeBox.addItem(info.getDisplay(), String.valueOf(info.getId()), info.getType());
                break;
            }
        }
    }

    public void setAccountData(LinkedHashMap<Long, String> data) {
        if (data == null || data.isEmpty())
            return;

        this.accountList.clear();
        for (Map.Entry<Long, String> entry : data.entrySet()) {
            String name = entry.getValue();

            if (name.trim().isEmpty())
                continue;
            accountList.addItem(name, String.valueOf(entry.getKey()));
        }
    }

    public void setGroupData(LinkedHashMap<Long, String> data) {
        if (data == null || data.isEmpty())
            return;

        this.groupList.clear();
        for (Long id : data.keySet()) {
            String name = data.get(id);
            groupList.addItem(name, String.valueOf(id));
        }
    }

    @Override
    public HashMap<PermissionType, HashSet<String>> getReadSelected() {
        return readBox.getData();
    }

    @Override
    public HashMap<PermissionType, HashSet<String>> getWriteSelected() {
        return writeBox.getData();
    }

    // inner classes
    private class PermissionListBox implements IsWidget {
        private final ListBox listBox;
        private final HashMap<PermissionType, HashSet<String>> data;

        public PermissionListBox() {

            listBox = new ListBox(true);
            listBox.setSize("200px", "150px");
            listBox.setStyleName("input_box");
            data = new HashMap<PermissionType, HashSet<String>>();
        }

        public void addItem(String item, String value, PermissionType type) {

            HashSet<String> typeData = this.data.get(type);
            if (typeData == null) {
                typeData = new HashSet<String>();
                this.data.put(type, typeData);
            } else {
                // already added
                if (typeData.contains(value))
                    return;
            }

            typeData.add(value);
            listBox.addItem(item, value);
        }

        public void removeSelected() {
            int i = listBox.getSelectedIndex();
            // TODO : cannot remove from list

            while (i != -1) {
                String value = listBox.getValue(i);

                if (data.remove(value) != null)
                    listBox.removeItem(i);
                i = listBox.getSelectedIndex();
            }
        }

        public HashMap<PermissionType, HashSet<String>> getData() {
            return this.data;
        }

        @Override
        public Widget asWidget() {
            return listBox;
        }
    }

    /**
     * ClickHandler implementation for adding items from the main
     * lists (accounts and groups) to the read and write boxes
     */
    private class AddToClickHandler implements ClickHandler {

        private final boolean isWrite;

        public AddToClickHandler(boolean isWrite) {
            this.isWrite = isWrite;
        }

        @Override
        public void onClick(ClickEvent event) {
            int selected = tabPanel.getSelectedIndex();

            if (selected == 0) {
                // user selected
                int i = accountList.getSelectedIndex();
                if (i == -1)
                    return;

                for (; i < accountList.getItemCount(); i += 1) {
                    if (accountList.isItemSelected(i)) {
                        readBox.addItem(accountList.getItemText(i), accountList.getValue(i),
                            PermissionType.READ_ACCOUNT);
                        if (isWrite) {
                            writeBox.addItem(accountList.getItemText(i), accountList.getValue(i),
                                PermissionType.WRITE_ACCOUNT);
                        }
                    }
                }
            } else {
                // group tab selected
                int j = groupList.getSelectedIndex();
                if (j == -1)
                    return;

                for (; j < groupList.getItemCount(); j += 1) {
                    if (groupList.isItemSelected(j)) {
                        readBox.addItem(groupList.getItemText(j), groupList.getValue(j),
                            PermissionType.READ_GROUP);
                        if (isWrite) {
                            writeBox.addItem(groupList.getItemText(j), groupList.getValue(j),
                                PermissionType.WRITE_GROUP);
                        }
                    }
                }
            }
        }
    }
}
