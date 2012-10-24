package org.jbei.ice.client.collection.widget;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog widget for sharing collections
 *
 * @author Hector Plahar
 */
public class ShareCollectionWidget extends Composite {
    private final long collectionId;
    private final String collectionName;
    private Button share;
    private Button cancel;
    private Icon closeIcon;
    private PopupPanel box;

    public ShareCollectionWidget(long collectionId, String collectionName) {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        initWidget(layout);

        layout.setStyleName("add_to_popup");
        layout.addStyleName("pad-4");
        layout.addStyleName("bg_white");

        this.collectionId = collectionId;
        this.collectionName = collectionName;
        initComponents();

        // set Widgets
        layout.setWidget(0, 0, createHeader());
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);
        layout.setWidget(1, 0, share);
        layout.setWidget(1, 1, cancel);
    }

    protected Widget createHeader() {
        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);

        table.setHTML(0, 0, "Share <b><i>" + collectionName + "</b></i>");
        table.setWidget(0, 1, closeIcon);

        return table;
    }

    protected void initComponents() {
        ClickHandler closeHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                box.hide();
            }
        };

        share = new Button("Share");
        cancel = new Button("Cancel");
        cancel.addClickHandler(closeHandler);
        closeIcon = new Icon(FAIconType.REMOVE_CIRCLE);
        closeIcon.addClickHandler(closeHandler);

        box = new PopupPanel();
        box.setWidth("600px");
        box.setModal(true);
        box.setGlassEnabled(true);
        box.setGlassStyleName("dialog_box_glass");
        box.setWidget(this);
    }

    public void showDialog() {
        box.center();
    }
}
