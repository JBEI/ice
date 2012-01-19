package org.jbei.ice.client.collection.model;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextBox;

public class CreateCollectionPanel extends Composite {

    private final Button submitCollectionButton;
    private final Button cancelAddCollectionButton;
    private final TextBox nameInput;
    private final TextBox descriptionInput;

    public CreateCollectionPanel() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(3);
        layout.setCellSpacing(0);

        layout.setWidth("95%");
        initWidget(layout);

        FlexCellFormatter formatter = layout.getFlexCellFormatter();

        // name label
        layout.setHTML(0, 0, "Name <span class=\"required\">*</span>");
        formatter.setWidth(0, 0, "70px");
        formatter.setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);

        // name input
        nameInput = new TextBox();
        nameInput.addStyleName("input_box");
        layout.setWidget(0, 1, nameInput);

        // description label
        layout.setHTML(0, 2, "Description");
        formatter.setWidth(0, 2, "90px");

        // description input
        descriptionInput = new TextBox();
        descriptionInput.addStyleName("input_box");
        descriptionInput.setWidth("640px");
        layout.setWidget(0, 3, descriptionInput);
        formatter.setWidth(0, 3, "55%");

        // submit button
        submitCollectionButton = new Button("Submit");
        layout.setWidget(0, 4, submitCollectionButton);
        formatter.setWidth(0, 4, "50px");

        // cancel button
        cancelAddCollectionButton = new Button("Cancel");
        layout.setWidget(0, 5, cancelAddCollectionButton);
        formatter.setWidth(0, 5, "60px");
    }

    public void reset() {
        this.nameInput.setText("");
        this.nameInput.setStyleName("input_box");
        this.descriptionInput.setText("");
        this.descriptionInput.setStyleName("input_box");
    }

    /**
     * Associates handler passed in the param to the submit button.
     * Also validates the nameInput to ensure that the user has entered a value
     * 
     * @param handler
     *            ClickHandler to associate with the submit button
     */
    public void addSubmitHandler(ClickHandler handler) {
        submitCollectionButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (nameInput.getText().isEmpty()) {
                    nameInput.setStyleName("entry_input_error");
                    nameInput.setFocus(true);
                } else
                    nameInput.setStyleName("input_box");
            }
        });

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
