package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;

import org.jbei.ice.client.entry.view.model.PermissionSuggestOracle;
import org.jbei.ice.client.entry.view.view.PermissionsPresenter.IPermissionsView;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for displaying the entry permissions
 * 
 * @author Hector Plahar
 */

public class PermissionsWidget extends Composite implements IPermissionsView {

    private final FlexTable layout;
    private final Tree readTree;
    private final Tree rwTree;
    private final TreeItem readRoot;
    private final TreeItem rwRoot;
    private final Label readAddLabel;
    private final Label writeAddLabel;
    private final PermissionsPresenter presenter;
    private final SuggestBox readSuggestBox;
    private final SuggestBox writeSuggestBox;
    private final TreeItem readItemBoxHolder;
    private final TreeItem writeItemBoxHolder;

    public PermissionsWidget() {
        layout = new FlexTable();
        initWidget(layout);

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("permissions_display");

        // read
        readAddLabel = new Label("Add");
        readAddLabel.setStyleName("edit_permissions_label");
        readAddLabel.addStyleName("display-inline");

        // read/write
        writeAddLabel = new Label("Add");
        writeAddLabel.setStyleName("edit_permissions_label");
        writeAddLabel.addStyleName("display-inline");

        layout.setHTML(0, 0, "Permissions");
        layout.getCellFormatter().setStyleName(0, 0, "permissions_sub_header");

        readSuggestBox = new SuggestBox(new PermissionSuggestOracle());
        readSuggestBox.setWidth("130px");
        readSuggestBox.setStyleName("permission_input_suggest");
        readSuggestBox.setLimit(7);

        writeSuggestBox = new SuggestBox(new PermissionSuggestOracle());
        writeSuggestBox.setWidth("130px");
        writeSuggestBox.setStyleName("permission_input_suggest");
        writeSuggestBox.setLimit(7);

        readItemBoxHolder = new TreeItem(readSuggestBox);
        readItemBoxHolder.setVisible(false);
        writeItemBoxHolder = new TreeItem(writeSuggestBox);
        writeItemBoxHolder.setVisible(false);

        readTree = new Tree();
        readTree.setAnimationEnabled(true);
        readTree.setStyleName("font-75em");

        readRoot = new TreeItem(createReadRoot());
        readTree.addItem(readRoot);
        readRoot.addItem(readItemBoxHolder);

        rwTree = new Tree();
        rwTree.setAnimationEnabled(true);
        rwTree.setStyleName("font-75em");
        rwRoot = new TreeItem(createWriteRoot());

        rwRoot.addItem(writeItemBoxHolder);
        rwTree.addItem(rwRoot);

        layout.setWidget(1, 0, readTree);
        layout.setWidget(2, 0, rwTree);

        rwRoot.setState(true, false);
        readRoot.setState(true, false);

        presenter = new PermissionsPresenter(this);
    }

    private Widget createReadRoot() {
        HTMLPanel panel = new HTMLPanel(
                "Read Allowed<span style=\"float:right; margin-left: 15px\" id=\"permissions_read_allowed_add_link\"></span>");
        panel.add(readAddLabel, "permissions_read_allowed_add_link");
        return panel;
    }

    private Widget createWriteRoot() {
        HTMLPanel panel = new HTMLPanel(
                "Write Allowed<span style=\"float:right; margin-left: 15px\" id=\"permissions_write_allowed_add_link\"></span>");
        panel.add(writeAddLabel, "permissions_write_allowed_add_link");
        return panel;
    }

    @Override
    public HandlerRegistration addReadBoxSelectionHandler(
            SelectionHandler<SuggestOracle.Suggestion> handler) {
        return readSuggestBox.addSelectionHandler(handler);
    }

    @Override
    public HandlerRegistration addWriteBoxSelectionHandler(
            SelectionHandler<SuggestOracle.Suggestion> handler) {
        return writeSuggestBox.addSelectionHandler(handler);
    }

    @Override
    public HandlerRegistration setReadAddClickHandler(ClickHandler handler) {
        return readAddLabel.addClickHandler(handler);
    }

    @Override
    public HandlerRegistration setWriteAddClickHandler(ClickHandler handler) {
        return writeAddLabel.addClickHandler(handler);
    }

    @Override
    public void setWriteBoxVisibility(boolean visible) {
        writeItemBoxHolder.setVisible(visible);
        if (visible)
            writeSuggestBox.getTextBox().setFocus(true);
    }

    @Override
    public void setReadBoxVisibility(boolean visible) {
        readItemBoxHolder.setVisible(visible);
        if (visible)
            readSuggestBox.getTextBox().setFocus(true);
    }

    public void setPermissionData(ArrayList<PermissionInfo> data) {

        ArrayList<PermissionItem> itemList = new ArrayList<PermissionItem>();

        for (PermissionInfo info : data) {
            PermissionItem item = null;
            switch (info.getType()) {
            case READ_ACCOUNT:
                item = new PermissionItem(info.getId(), info.getDisplay(), false, false);
                break;

            case READ_GROUP:
                item = new PermissionItem(info.getId(), info.getDisplay(), true, false);
                break;

            case WRITE_ACCOUNT:
                item = new PermissionItem(info.getId(), info.getDisplay(), false, true);
                break;

            case WRITE_GROUP:
                item = new PermissionItem(info.getId(), info.getDisplay(), true, true);
                break;
            }

            if (item != null)
                itemList.add(item);
        }
        setPermissionItems(itemList);
    }

    private void setPermissionItems(ArrayList<PermissionItem> data) {

        if (data == null)
            return;

        for (PermissionItem datum : data) {
            if (datum.isWrite())
                rwRoot.addItem(datum.getName());
            else
                readRoot.addItem(datum.getName());
        }
    }

    @Override
    public void addReadItem(PermissionItem item) {
        readRoot.addItem(item.getName());
    }

    @Override
    public void addWriteItem(PermissionItem item) {
        rwRoot.addItem(item.getName());
    }

    public PermissionsPresenter getPresenter() {
        return presenter;
    }
}
