package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Header widget for the collection menu. Renders the header label and requested icons
 * such as the quick add icon
 *
 * @author Hector Plahar
 */
public class MenuHeader extends Composite {

    private Icon expandCollapseIcon;    // icon that indicates whether collection has been expanded or collapsed
    private final Label headerLabel;
    private HTML quickAddIcon;
    private boolean collapsed; // this should be saved in a cookie or something

    public MenuHeader(String header, boolean addQuickEdit) {
        expandCollapseIcon = new Icon(FAIconType.FOLDER_OPEN);
        headerLabel = new Label(header);
        headerLabel.setStyleName("display-inline");
        headerLabel.addStyleName("cursor_pointer");
        // quick collection add
        quickAddIcon = new HTML("<i class=\"" + FAIconType.PLUS_CIRCLE.getStyleName() + " font-12em\"></i>");
        quickAddIcon.setVisible(addQuickEdit);
        quickAddIcon.addStyleName("edit_icon");
        quickAddIcon.setTitle("Create a new collection");

        HTMLPanel menuHeaderPanel = new HTMLPanel(
                "<span style=\"float: left; opacity: 0.65;\" "
                        + " id=\"expand_collapse\"></span>&nbsp;&nbsp;"
                        + "<span id=\"collection_menu_header\"></span>"
                        + "<span style=\"float: right\" id=\"quick_add\"></span>");
        menuHeaderPanel.add(expandCollapseIcon, "expand_collapse");
        menuHeaderPanel.add(headerLabel, "collection_menu_header");
        menuHeaderPanel.add(quickAddIcon, "quick_add");
        initWidget(menuHeaderPanel);
    }

    public void addQuickAddHandler(ClickHandler handler) {
        quickAddIcon.addClickHandler(handler);
    }

    public void addExpandCollapseHandler(final ClickHandler handler) {
        ClickHandler newClickHandler = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (collapsed) {
                    expandCollapseIcon.setType(FAIconType.FOLDER_OPEN);
                } else {
                    expandCollapseIcon.setType(FAIconType.FOLDER_CLOSE);
                }
                collapsed = !collapsed;
                handler.onClick(event);
            }
        };

        headerLabel.addClickHandler(newClickHandler);
        expandCollapseIcon.addClickHandler(newClickHandler);
    }

    public boolean isCollapsed() {
        return this.collapsed;
    }
}
