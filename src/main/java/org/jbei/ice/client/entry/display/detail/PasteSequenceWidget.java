package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.client.common.widget.GenericPopup;
import org.jbei.ice.client.common.widget.ICanReset;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that facilitates associating a sequence with an entry via pasting/entering
 * the raw sequence
 *
 * @author Hector Plahar
 */
public class PasteSequenceWidget implements ICanReset {

    private TextArea area;
    private HandlerRegistration saveRegistration;
    private final GenericPopup popup;

    public PasteSequenceWidget() {
        initComponents();
        popup = new GenericPopup(this, "<b class=\"font-95em\">Paste Sequence</b>");
    }

    private void initComponents() {
        area = new TextArea();
        area.setStyleName("input_box");
        area.setWidth("100%");
        area.setHeight("200px");
    }

    public void addSaveHandler(final ClickHandler handler) {
        if (saveRegistration != null)
            saveRegistration.removeHandler();

        saveRegistration = popup.addSaveButtonHandler(new ClickHandler() {

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
        popup.showDialog();
    }

    public void hideDialog() {
        popup.hideDialog();
    }

    @Override
    public void reset() {
        area.setText("");
    }

    @Override
    public Widget asWidget() {
        return area;
    }
}
