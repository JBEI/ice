package org.jbei.ice.client.common.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wraps a {@link com.google.gwt.user.client.ui.PopupPanel} to enable
 * a common style for popups site wide
 *
 * @author Hector Plahar
 */
public class GenericPopup extends Composite {

    private final FlexTable layout;
    private HTML close;
    private PopupPanel box;
    private Button saveButton;
    private ICanReset resetWidget;
    private HTML cancel;

    public GenericPopup(ICanReset widget, String title) {
        layout = new FlexTable();
        initWidget(layout);
        this.resetWidget = widget;

        // initialise components used in this widget
        initComponents();

        layout.setWidget(0, 0, createHeader(title));
        layout.setWidget(1, 0, resetWidget.asWidget());

        String html = "<span id=\"save_widget\"></span> &nbsp; <span id=\"cancel_widget\"></span>";
        HTMLPanel saveCancelPanel = new HTMLPanel(html);
        saveCancelPanel.add(saveButton, "save_widget");
        saveCancelPanel.add(cancel, "cancel_widget");
        layout.setWidget(2, 0, saveCancelPanel);
        layout.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasAlignment.ALIGN_RIGHT);
        layout.getRowFormatter().setVisible(2, false);
    }

    public void showDialog() {
        box.center();
    }

    public void hideDialog() {
        resetWidget.reset();
        box.hide();
    }

    protected void initComponents() {
        layout.setWidth("100%");
        layout.setStyleName("add_to_popup");
        layout.addStyleName("pad-8");
        layout.addStyleName("bg_white");

        ClickHandler closeHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                box.hide();
            }
        };

        close = new HTML("<i class=\"" + FAIconType.REMOVE_SIGN.getStyleName() + "\"></i> Close");
        close.setStyleName("opacity_hover");
        close.addStyleName("font-75em");
        close.addClickHandler(closeHandler);

        box = new PopupPanel();
        box.setWidth("600px");
        box.setModal(true);
        box.setGlassEnabled(true);
        box.setGlassStyleName("dialog_box_glass");
        box.setWidget(this);

        cancel = new HTML("Cancel");
        cancel.setStyleName("display-inline");
        cancel.addStyleName("footer_feedback_widget");
        cancel.addStyleName("font-75em");
        cancel.addClickHandler(closeHandler);

        saveButton = new Button("Save");

        box.setWidget(this);
    }

    public HandlerRegistration addSaveButtonHandler(ClickHandler handler) {
        layout.getRowFormatter().setVisible(2, handler != null);
        return saveButton.addClickHandler(handler);
    }

    protected Widget createHeader(String htmlTitle) {
        HTMLPanel panel = new HTMLPanel(htmlTitle + " <span style=\"float: right\" id=\"close_panel\"></span>");
        panel.add(close, "close_panel");
        return panel;
    }
}
