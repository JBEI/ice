package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */
public class CreatorWidget implements IsWidget {

    private final FocusPanel parent;
    private final VerticalPanel panel;
    private TextBox creatorBox;
    private TextBox creatorEmailBox;
    private final String defaultCreator;
    private final String defaultCreatorEmail;

    public CreatorWidget(String creator, String creatorEmail) {
        Icon creatorIcon = new Icon(FAIconType.USER);
        creatorIcon.setTitle("Click to set creator information");
        parent = new FocusPanel(creatorIcon);
        parent.setStyleName("bulk_upload_creator");

        panel = new VerticalPanel();
        panel.setStyleName("bg_white");
        this.defaultCreator = creator;
        this.defaultCreatorEmail = creatorEmail;
        createTableContents();

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

    private void createTableContents() {
        addLabel(true, "Creator");
        creatorBox = new TextBox();
        creatorBox.getElement().setAttribute("placeholder", "Who made this part?");
        creatorBox.setWidth("150px");
        creatorBox.setText(defaultCreator);
        creatorBox.setStyleName("input_box");
        creatorBox.setMaxLength(65);
        panel.add(creatorBox);

        addLabel(false, "Creator's Email");
        creatorEmailBox = new TextBox();
        creatorEmailBox.setText(defaultCreatorEmail);
        creatorEmailBox.setStyleName("input_box");
        creatorEmailBox.setWidth("150px");
        creatorEmailBox.setMaxLength(75);
        panel.add(creatorEmailBox);
    }

    protected void addLabel(boolean required, String label) {
        String html = "<span class=\"font-70em\" style=\"white-space:nowrap\"><b>" + label + "</b>";
        if (required)
            html += " <span class=\"required\">*</span></span>";
        else
            html += "</span>";

        HTML widget = new HTML(html);
        panel.add(widget);
    }

    public String getCreator() {
        String creator = this.creatorBox.getText();
        if (creator.trim().isEmpty())
            creator = defaultCreator;

        return creator;
    }

    public String getCreatorEmail() {
        String creatorEmail = this.creatorEmailBox.getText();
        if (creatorEmail.trim().isEmpty())
            creatorEmail = defaultCreatorEmail;

        return creatorEmail;
    }

    /**
     * Returns the {@link com.google.gwt.user.client.ui.Widget} aspect of the receiver.
     */
    @Override
    public Widget asWidget() {
        return parent;
    }
}
