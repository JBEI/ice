package org.jbei.ice.client.bulkimport.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SaveDraftInput extends Composite {

    private final Button saveDraftButton;
    private final TextBox inputName;

    public SaveDraftInput() {
        saveDraftButton = new Button("Save Draft");
        saveDraftButton.setStyleName("saved_draft_button");
        inputName = new TextBox();
        inputName.setStyleName("saved_draft_input");
        String html = "<span id=\"save_draft_inputbox\"></span>&nbsp;<span id=\"save_draft_button\"></span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.setStyleName("display-inline");
        initWidget(panel);

        panel.add(inputName, "save_draft_inputbox");
        panel.add(saveDraftButton, "save_draft_button");
    }

    public String getDraftName() {
        return inputName.getText();
    }

    public void addSaveDraftHandler(final ClickHandler handler) {

        saveDraftButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (getDraftName().isEmpty()) {
                    inputName.setStyleName("saved_draft_input_error");
                } else {
                    inputName.setStyleName("saved_draft_input");
                    handler.onClick(event);
                }
            }
        });
    }
}
