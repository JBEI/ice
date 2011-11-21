package org.jbei.ice.client.collection.model;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class CreateCollectionPanel extends Composite {

    private final Button submitCollectionButton;
    private final Button cancelAddCollectionButton;
    private final TextBox nameInput;
    private final TextBox descriptionInput;

    public CreateCollectionPanel() {
        HorizontalPanel panel = new HorizontalPanel();
        initWidget(panel);

        submitCollectionButton = new Button("Submit");
        cancelAddCollectionButton = new Button("Cancel");

        panel.add(new Label("Name"));
        nameInput = new TextBox();

        panel.add(nameInput);
        panel.add(new Label("Description"));
        descriptionInput = new TextBox();
        panel.add(descriptionInput);
        panel.add(submitCollectionButton);
        panel.add(cancelAddCollectionButton);
    }

    public void reset() {

    }

    public void addSubmitHandler(ClickHandler handler) {
        submitCollectionButton.addClickHandler(handler);
    }

    public void addCancelHandler(ClickHandler handler) {
        cancelAddCollectionButton.addClickHandler(handler);
    }

    public String getCollectionName() {
        return this.nameInput.getText();
    }

    public String getCollectionDescription() {
        return this.descriptionInput.getText();
    }
}
