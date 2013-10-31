package org.jbei.ice.client.common.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Essentially a wrapper around the gwt DialogBox to enable styling and interaction
 * conforms to ICE
 *
 * @author Hector Plahar
 */
public class Dialog {

    private final DialogBox dialogBox;
    private final Label cancel;
    private final Button submitButton;
    private HandlerRegistration registration;

    public Dialog(Widget widget, String width, String header) {
        dialogBox = new DialogBox(new Caption());
        dialogBox.setStyleName("add_to_popup");
        dialogBox.addStyleName("bg_white");
        dialogBox.setWidth(width);
        dialogBox.setGlassEnabled(true);
        dialogBox.setGlassStyleName("dialog_box_glass");
        if (header != null && !header.trim().isEmpty()) {
            String html = "<div style=\"padding: 8px; font-weight: bold; font-size: 0.80em; background-color: white\">"
                    + header + "</div>";
            dialogBox.setHTML(html);
        }

        cancel = new Label("Close");
        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-80em");
        cancel.addStyleName("display-inline");

        submitButton = new Button("Submit");
        submitButton.setVisible(false);

        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        dialogBox.setWidget(createWrapperWidget(widget));
    }

    public void showDialog(boolean show) {
        if (show)
            dialogBox.center();
        else
            dialogBox.hide();
    }

    public void setSubmitHandler(ClickHandler handler) {
        if (registration != null)
            registration.removeHandler();

        registration = submitButton.addClickHandler(handler);
        submitButton.setVisible(true);
        cancel.setText("Cancel");
    }

    protected Widget createWrapperWidget(Widget widget) {
        FlexTable table = new FlexTable();
        table.setWidget(0, 0, widget);
        table.setWidget(1, 0, createActionWidget());
        table.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_RIGHT);
        return table;
    }

    protected Widget createActionWidget() {
        String html = "<span id=\"submit_action\"></span> &nbsp; <span id=\"cancel_action\"></span>";
        HTMLPanel htmlPanel = new HTMLPanel(html);
        htmlPanel.add(submitButton, "submit_action");
        htmlPanel.add(cancel, "cancel_action");
        return htmlPanel;
    }

    /**
     * Caption for the dialog box.
     */
    private class Caption extends HTML implements DialogBox.Caption {
    }
}
