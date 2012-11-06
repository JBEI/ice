package org.jbei.ice.client.bulkupload.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Hector Plahar
 */
public class UpdateDraftInput extends Composite {
    private final Label inputName;
    private final Button updateButton;
    private HandlerRegistration registration;
    private final Button saveButton;

    public UpdateDraftInput() {
        inputName = new Label();
        updateButton = new Button("Update");
        updateButton.setStyleName("saved_draft_button");

        FlexTable layout = new FlexTable();
        layout.setStyleName("display-inline");
        String html = "<span id=\"save_draft_inputbox\"></span>&nbsp;<span id=\"save_draft_button\" style=\"float: " +
                "right\"></span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.setStyleName("display-inline");
        initWidget(layout);

        panel.add(inputName, "save_draft_inputbox");
        panel.add(updateButton, "save_draft_button");
        layout.setWidget(0, 0, inputName);
        layout.setWidget(0, 1, updateButton);

        saveButton = new Button("Submit");
        saveButton.setStyleName("saved_draft_button");
    }

    public void setUpdateDraftHandler(final ClickHandler handler) {
        if (registration != null)
            registration.removeHandler();

        registration = updateButton.addClickHandler(handler);
    }

    public void setDraftName(String name) {
        inputName.setText(name);
    }

    public Button getSaveButton() {
        return this.saveButton;
    }
}
