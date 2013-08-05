package org.jbei.ice.client.entry.display.detail;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Widget that facilitates associating a sequence with an entry via copy paste
 *
 * @author Hector Plahar
 */
public class PasteSequenceWidget extends FlexTable {

    private Button saveButton;
    private Button cancelButton;
    private TextArea area;
    private DialogBox box;
    private HandlerRegistration saveRegistration;

    public PasteSequenceWidget() {
        setWidth("100%");
        setCellPadding(0);
        setCellSpacing(0);

        initComponents();

        setWidget(0, 0, area);
        getFlexCellFormatter().setColSpan(0, 0, 2);

        setWidget(1, 0, saveButton);
        getCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_RIGHT);
        setWidget(1, 1, cancelButton);
        getCellFormatter().setWidth(1, 1, "70px");
    }

    private void initComponents() {
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                area.setText("");
                box.hide();
            }
        });

        area = new TextArea();
        area.setStyleName("input_box");
        area.setWidth("600px");
        area.setHeight("200px");

        box = new DialogBox();
        box.setWidth("620px");
        box.setModal(true);
        box.setHTML("Paste Sequence");
        box.setGlassEnabled(true);
        box.setWidget(this);
    }

    public void addSaveHandler(final ClickHandler handler) {
        if (saveRegistration != null)
            saveRegistration.removeHandler();

        saveRegistration = saveButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (area.getText().trim().isEmpty()) {
                    area.setStyleName("input_box_error");
                    return;
                }

                area.setStyleName("input_box");
                handler.onClick(event);
            }
        });
    }

    public String getSequence() {
        return area.getText();
    }

    public void showDialog() {
        area.setText("");
        box.center();
    }

    public void hideDialog() {
        area.setText("");
        box.hide();
    }
}
