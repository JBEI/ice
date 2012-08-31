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
 * @author Hector Plahar
 */
public class CreatorWidget implements IsWidget {

    private final FocusPanel parent;
    private final HTMLPanel panel;
    private TextBox creatorBox;
    private TextBox creatorEmailBox;

    public CreatorWidget(String creator, String creatorEmail) {
        Icon creatorIcon = new Icon(FAIconType.USER);
        creatorIcon.setTitle("Click to set creator information");
        parent = new FocusPanel(creatorIcon);
        parent.setStyleName("bulk_upload_creator");

        panel = new HTMLPanel("<span id=\"creator_label\"></span><br><span id=\"creator_input\"></span><br><br>" +
                                      "<span id=\"creator_email_label\"></span><br><span " +
                                      "id=\"creator_email_input\"></span>");
        panel.setStyleName("bg_white");
        panel.addStyleName("pad-6");
        createTableContents(creator, creatorEmail);

        final PopupHandler clickHandler = new PopupHandler(panel, creatorIcon.getElement(), 0, 0, false);
        clickHandler.setCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (creatorBox.getText().isEmpty()) {
                    creatorBox.setStyleName("entry_input_error");
                    clickHandler.showPopup();
                } else {
                    creatorBox.setStyleName("input_box");
                }
            }
        });
        parent.addClickHandler(clickHandler);
    }

    private void createTableContents(String creator, String creatorEmail) {
        addLabel(true, "Creator", "creator_label");
        creatorBox = new TextBox();
        creatorBox.getElement().setAttribute("placeholder", "Who made this part?");
        creatorBox.setWidth("150px");
        creatorBox.setText(creator);
        creatorBox.setStyleName("input_box");
        creatorBox.setMaxLength(65);

        panel.add(creatorBox, "creator_input");

        addLabel(false, "Creator's Email", "creator_email_label");
        creatorEmailBox = new TextBox();
        creatorEmailBox.setText(creatorEmail);
        creatorEmailBox.setStyleName("input_box");
        creatorEmailBox.setWidth("150px");
        creatorEmailBox.setMaxLength(75);
        panel.add(creatorEmailBox, "creator_email_input");
    }

    protected void addLabel(boolean required, String label, String elementId) {
        String html = "<span class=\"font-70em\" style=\"white-space:nowrap\">" + label;
        if (required)
            html += " <span class=\"required\">*</span></span>";
        else
            html += "</span>";

        HTML widget = new HTML(html);
        widget.setStyleName("display-inline");
        panel.add(widget, elementId);
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
