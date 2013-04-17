package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for setting/displaying the entry creator information
 *
 * @author Hector Plahar
 */
public class CreatorWidget implements IsWidget {

    private final FocusPanel parent;
    private final HTMLPanel popUp;
    private TextBox creatorBox;
    private TextBox creatorEmailBox;

    public CreatorWidget(String creator, String creatorEmail) {
        Icon creatorIcon = new Icon(FAIconType.USER);
        creatorIcon.addStyleName("display-inline");
        creatorIcon.removeStyleName("font-awesome");

        HTMLPanel creatorPanel = new HTMLPanel("<span id=\"creator_icon\"></span> Creator <i class=\""
                                                       + FAIconType.CARET_DOWN.getStyleName() + "\"></i>");
        creatorPanel.add(creatorIcon, "creator_icon");
        creatorPanel.setStyleName("display-inline");
        creatorPanel.setTitle("Person who made this part");

        parent = new FocusPanel(creatorPanel);
        parent.setStyleName("bulk_upload_creator");
        parent.addStyleName("opacity_hover");

        popUp = new HTMLPanel("<span id=\"creator_label\"></span><br><span id=\"creator_input\"></span><br><br>" +
                                      "<span id=\"creator_email_label\"></span><br><span " +
                                      "id=\"creator_email_input\"></span>");
        popUp.setStyleName("bg_fc");
        popUp.addStyleName("pad-6");
        createTableContents(creator, creatorEmail);

        final PopupHandler popUp = new PopupHandler(this.popUp, creatorIcon.getElement(), false);
        popUp.setCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (creatorBox.getText().isEmpty()) {
                    creatorBox.setStyleName("input_box_error");
                    popUp.show();
                } else {
                    creatorBox.setStyleName("input_box");
                }
            }
        });
        parent.addClickHandler(popUp);
    }

    private void createTableContents(String creator, String creatorEmail) {
        addLabel(true, "Creator", "creator_label");
        creatorBox = new TextBox();
        creatorBox.getElement().setAttribute("placeholder", "Who made this part?");
        creatorBox.setWidth("150px");
        creatorBox.setText(creator);
        creatorBox.setStyleName("input_box");
        creatorBox.setMaxLength(65);

        popUp.add(creatorBox, "creator_input");

        addLabel(false, "Creator's Email", "creator_email_label");
        creatorEmailBox = new TextBox();
        creatorEmailBox.setText(creatorEmail);
        creatorEmailBox.setStyleName("input_box");
        creatorEmailBox.setWidth("150px");
        creatorEmailBox.setMaxLength(75);
        popUp.add(creatorEmailBox, "creator_email_input");
    }

    protected void addLabel(boolean required, String label, String elementId) {
        String html = "<span class=\"font-70em\" style=\"white-space:nowrap\">" + label;
        if (required)
            html += " <span class=\"required\">*</span></span>";
        else
            html += "</span>";

        HTML widget = new HTML(html);
        widget.setStyleName("display-inline");
        popUp.add(widget, elementId);
    }

    public String getCreator() {
        return this.creatorBox.getText().trim();
    }

    public String getCreatorEmail() {
        return this.creatorEmailBox.getText().trim();
    }

    public void setCreator(String creator) {
        this.creatorBox.setText(creator);
    }

    public void setCreatorEmail(String email) {
        this.creatorEmailBox.setText(email);
    }

    /**
     * Returns the {@link com.google.gwt.user.client.ui.Widget} aspect of the receiver.
     */
    @Override
    public Widget asWidget() {
        return parent;
    }
}
