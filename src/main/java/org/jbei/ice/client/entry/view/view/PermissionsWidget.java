package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.entry.view.model.PermissionSuggestOracle;
import org.jbei.ice.client.entry.view.view.PermissionsPresenter.IPermissionsView;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;

/**
 * Widget for displaying the entry permissions
 *
 * @author Hector Plahar
 */

public class PermissionsWidget extends Composite implements IPermissionsView {

    private final TreeItem readRoot;
    private final TreeItem rwRoot;
    private Label readAddLabel;
    private Label writeAddLabel;
    private final PermissionsPresenter presenter;
    private final SuggestBox readSuggestBox;
    private final SuggestBox writeSuggestBox;
    private final TreeItem readItemBoxHolder;
    private final TreeItem writeItemBoxHolder;

    public PermissionsWidget() {
        FlexTable layout = new FlexTable();
        initWidget(layout);

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("entry_attribute");

        initComponents();

        layout.setHTML(0, 0, "<i class=\"" + FAIconType.KEY.getStyleName() + " font-80em\"></i> &nbsp;Permissions");
        layout.getCellFormatter().setStyleName(0, 0, "entry_attributes_sub_header");

        readSuggestBox = new SuggestBox(new PermissionSuggestOracle());
        readSuggestBox.setWidth("130px");
        readSuggestBox.setStyleName("permission_input_suggest");
        readSuggestBox.getValueBox().getElement().setAttribute("placeHolder", "User or Group name");
        readSuggestBox.setLimit(7);

        writeSuggestBox = new SuggestBox(new PermissionSuggestOracle());
        writeSuggestBox.setWidth("130px");
        writeSuggestBox.setStyleName("permission_input_suggest");
        writeSuggestBox.getValueBox().getElement().setAttribute("placeHolder", "User or Group name");
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
        HTMLPanel readPanel = new HTMLPanel(
                "Read Allowed<span style=\"margin-left: 15px\" id=\"permissions_read_allowed_add_link\"></span>");
        readPanel.add(readAddLabel, "permissions_read_allowed_add_link");
        return readPanel;
    }

    private Widget createWriteRoot() {
        HTMLPanel writePanel = new HTMLPanel(
                "Write Allowed<span style=\"margin-left: 15px\" id=\"permissions_write_allowed_add_link\"></span>");
        writePanel.add(writeAddLabel, "permissions_write_allowed_add_link");
        return writePanel;
    }

    /**
     * Adds links that allows user to modify permissions.
     * User should have write access
     *
     * @param showEdit allow permissions edit
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
            writeSuggestBox.getValueBox().setFocus(true);
        } else
            writeAddLabel.setText("Add");
    }

    @Override
    public void setReadBoxVisibility(boolean visible) {
        readItemBoxHolder.setVisible(visible);
        if (visible) {
            readAddLabel.setText("Close");
            readSuggestBox.setText("");
            readSuggestBox.getValueBox().setFocus(true);
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
    public void addReadItem(final PermissionInfo item, final Delegate<PermissionInfo> deleteDelegate) {
        ClickHandler clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (deleteDelegate != null)
                    deleteDelegate.execute(item);
            }
        };

        final TreeNode node = new TreeNode(item, clickHandler);
        if (deleteDelegate != null) {
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
    public void addWriteItem(final PermissionInfo item, final Delegate<PermissionInfo> deleteDelegate) {

        ClickHandler clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (deleteDelegate != null)
                    deleteDelegate.execute(item);
            }
        };

        final TreeNode node = new TreeNode(item, clickHandler);
        if (deleteDelegate != null) {
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
    public void removeReadItem(PermissionInfo item) {
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
    public void removeWriteItem(PermissionInfo item) {
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

        private final PermissionInfo item;

        public TreeNode(PermissionInfo item, ClickHandler deleteHandler) {
            super(new TreeNodeWidget(item.getDisplay(), deleteHandler,
                                     item.getArticle() == PermissionInfo.Article.GROUP, item.getArticleId()));
            this.item = item;
        }

        @Override
        public TreeNodeWidget getWidget() {
            return (TreeNodeWidget) super.getWidget();
        }

        public PermissionInfo getItem() {
            return this.item;
        }
    }

    private class TreeNodeWidget extends Composite implements HasMouseOverHandlers, HasMouseOutHandlers {

        private final HTML delete;

        public TreeNodeWidget(String display, ClickHandler handler, boolean isGroup, long id) {
            String html = "<span>";
            if (isGroup) {
                html += "<i class=\"font-90em " + FAIconType.GROUP.getStyleName() + "\" style=\"color: #bf984c\"></i>"
                        + "&nbsp;" + display;
            } else if (id > 0) {
                html += "<i class=\"font-90em " + FAIconType.USER.getStyleName() + "\" style=\"color: #657B83\"></i>"
                        + "&nbsp;<a href=\"#" + Page.PROFILE.getLink() + ";id=" + id + "\">" + display + "</a>";
            }

            html += "&nbsp; </span><span id=\"delete_link\" style=\"color: red; cursor: pointer\"></span>";

            HTMLPanel panel = new HTMLPanel(html);
            initWidget(panel);
            delete = new HTML("<i style=\"color: red\" class=\"" + FAIconType.TRASH.getStyleName()
                                      + " opacity_hover\"></i>");
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
