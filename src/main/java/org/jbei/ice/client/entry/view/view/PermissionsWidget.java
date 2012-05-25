package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.entry.view.model.PermissionSuggestOracle;
import org.jbei.ice.client.entry.view.view.PermissionsPresenter.IPermissionsView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
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
    private final TreeItem readRoot;
    private final TreeItem rwRoot;
    private Label readAddLabel;
    private Label writeAddLabel;
    private final PermissionsPresenter presenter;
    private final SuggestBox readSuggestBox;
    private final SuggestBox writeSuggestBox;
    private final TreeItem readItemBoxHolder;
    private final TreeItem writeItemBoxHolder;
    private HTMLPanel readPanel;
    private HTMLPanel writePanel;

    public PermissionsWidget() {
        layout = new FlexTable();
        initWidget(layout);

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("permissions_display");

        initComponents();

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

        Tree readTree = new Tree();
        readTree.setAnimationEnabled(true);
        readTree.setStyleName("font-75em");

        readRoot = new TreeItem(createReadRoot());
        readTree.addItem(readRoot);
        readRoot.addItem(readItemBoxHolder);

        Tree rwTree = new Tree();
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

    private void initComponents() {
        // read
        readAddLabel = new Label("Add");
        readAddLabel.setStyleName("edit_permissions_label");
        readAddLabel.addStyleName("display-inline");

        // read/write
        writeAddLabel = new Label("Add");
        writeAddLabel.setStyleName("edit_permissions_label");
        writeAddLabel.addStyleName("display-inline");
    }

    private Widget createReadRoot() {
        readPanel = new HTMLPanel(
                "Read Allowed<span style=\"margin-left: 15px\" id=\"permissions_read_allowed_add_link\"></span>");
        readPanel.add(readAddLabel, "permissions_read_allowed_add_link");
        return readPanel;
    }

    private Widget createWriteRoot() {
        writePanel = new HTMLPanel(
                "Write Allowed<span style=\"margin-left: 15px\" id=\"permissions_write_allowed_add_link\"></span>");
        writePanel.add(writeAddLabel, "permissions_write_allowed_add_link");
        return writePanel;
    }

    /**
     * Adds links that allows user to modify permissions.
     * User should have write access
     * 
     * @param showEdit
     */
    public void addReadWriteLinks(boolean showEdit) {
        readAddLabel.setVisible(showEdit);
        writeAddLabel.setVisible(showEdit);
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
        if (visible) {
            writeAddLabel.setText("Close");
            writeSuggestBox.setText("");
            writeSuggestBox.getTextBox().setFocus(true);
        } else
            writeAddLabel.setText("Add");
    }

    @Override
    public void setReadBoxVisibility(boolean visible) {
        readItemBoxHolder.setVisible(visible);
        if (visible) {
            readAddLabel.setText("Close");
            readSuggestBox.setText("");
            readSuggestBox.getTextBox().setFocus(true);
        } else
            readAddLabel.setText("Add");
    }

    @Override
    public void resetPermissionDisplay() {
        readRoot.removeItems();
        readRoot.addItem(readItemBoxHolder);

        rwRoot.removeItems();
        rwRoot.addItem(writeItemBoxHolder);
    }

    @Override
    public void setWidgetVisibility(boolean visible) {
        this.setVisible(visible);
    }

    @Override
    public void addReadItem(PermissionItem item, ClickHandler deleteHandler) {
        final TreeNode node = new TreeNode(item, deleteHandler);
        if (deleteHandler != null) {
            node.getWidget().addMouseOverHandler(new MouseOverHandler() {

                @Override
                public void onMouseOver(MouseOverEvent event) {
                    node.getWidget().setDeleteLinkVisible(true);
                }
            });

            node.getWidget().addMouseOutHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
                    node.getWidget().setDeleteLinkVisible(false);
                }
            });
        }
        readRoot.addItem(node);
    }

    @Override
    public void addWriteItem(PermissionItem item, ClickHandler deleteHandler) {
        final TreeNode node = new TreeNode(item, deleteHandler);
        if (deleteHandler != null) {
            node.getWidget().addMouseOverHandler(new MouseOverHandler() {

                @Override
                public void onMouseOver(MouseOverEvent event) {
                    node.getWidget().setDeleteLinkVisible(true);
                }
            });

            node.getWidget().addMouseOutHandler(new MouseOutHandler() {

                @Override
                public void onMouseOut(MouseOutEvent event) {
                    node.getWidget().setDeleteLinkVisible(false);
                }
            });
        }
        rwRoot.addItem(node);
    }

    @Override
    public void removeReadItem(PermissionItem item) {
        TreeItem toRemove = null;
        for (int i = 1; i < readRoot.getChildCount(); i += 1) {
            TreeNode node = (TreeNode) readRoot.getChild(i);
            if (node.getItem().equals(item)) {
                toRemove = node;
                break;
            }
        }

        if (toRemove != null)
            readRoot.removeItem(toRemove);
    }

    @Override
    public void removeWriteItem(PermissionItem item) {
        TreeItem toRemove = null;
        for (int i = 1; i < rwRoot.getChildCount(); i += 1) {
            TreeNode node = (TreeNode) rwRoot.getChild(i);
            if (node.getItem().equals(item)) {
                toRemove = node;
                break;
            }
        }

        if (toRemove != null)
            rwRoot.removeItem(toRemove);
    }

    public PermissionsPresenter getPresenter() {
        return presenter;
    }

    private class TreeNode extends TreeItem {

        private final PermissionItem item;

        public TreeNode(PermissionItem item, ClickHandler deleteHandler) {
            super(new TreeNodeWidget(item.getName(), deleteHandler, item.isGroup()));
            this.item = item;
        }

        @Override
        public TreeNodeWidget getWidget() {
            return (TreeNodeWidget) super.getWidget();
        }

        public PermissionItem getItem() {
            return this.item;
        }
    }

    private class TreeNodeWidget extends Composite implements HasMouseOverHandlers,
            HasMouseOutHandlers {
        private final Label delete;

        public TreeNodeWidget(String display, ClickHandler handler, boolean isGroup) {
            String html = "<span>";
            if (isGroup)
                html += "<img src=\"static/images/users16.png\" width=\"11px\" height=\"11px\" alt=\"Group\" /> &nbsp;";
            html += display
                    + " &nbsp; </span><span id=\"delete_link\" style=\"color: red; cursor: pointer\"></span>";

            HTMLPanel panel = new HTMLPanel(html);
            initWidget(panel);
            delete = new Label("x");
            delete.setStyleName("display-inline");
            delete.setVisible(false);

            if (handler != null) {
                panel.add(delete, "delete_link");
                delete.addClickHandler(handler);
            }
        }

        @Override
        public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
            return addDomHandler(handler, MouseOverEvent.getType());
        }

        public void setDeleteLinkVisible(boolean visible) {
            delete.setVisible(visible);
        }

        @Override
        public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
            return addDomHandler(handler, MouseOutEvent.getType());
        }
    }
}
