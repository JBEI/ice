package org.jbei.ice.client.entry.view.detail;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;

public class PasteSequenceWidget extends Composite {

    private Button saveButton;
    private Button cancelButton;
    private FlexTable table;
    private TextArea area;
    private DialogBox box;
    private HandlerRegistration saveRegistration;

    public PasteSequenceWidget() {
        table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);
        initWidget(table);

        initComponents();

        table.setWidget(1, 0, area);
        table.getFlexCellFormatter().setColSpan(1, 0, 2);

        table.setWidget(2, 0, saveButton);
        table.getCellFormatter().setHorizontalAlignment(2, 0, HasAlignment.ALIGN_RIGHT);
        table.setWidget(2, 1, cancelButton);
        table.getCellFormatter().setWidth(2, 1, "70px");

    }

    private void initComponents() {
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
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
}
