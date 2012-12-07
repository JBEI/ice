package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Widget for adding a new collection
 *
 * @author Hector Plahar
 */
public class QuickAddWidget extends Composite {

    private final TextBox quickAddBox;
    private final Button btnCancel;

    public QuickAddWidget(boolean resetOnFocus) {
        quickAddBox = new TextBox();
        quickAddBox.setStyleName("input_box");
        quickAddBox.getElement().setAttribute("placeholder", "Enter collection name");
        quickAddBox.setWidth("185px");
        quickAddBox.setMaxLength(35);

        if (resetOnFocus) {
            quickAddBox.addFocusHandler(new FocusHandler() {

                @Override
                public void onFocus(FocusEvent event) {
                    quickAddBox.setText("");
                }
            });
        }

        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        initWidget(layout);

        btnCancel = new Button("<i class=\"" + FAIconType.REMOVE.getStyleName() + "\"></i>");
        btnCancel.setStyleName("remove_filter");
        addCancelHandler();

        layout.setWidget(0, 0, quickAddBox);

        layout.setWidget(0, 1, btnCancel);
        layout.getFlexCellFormatter().setWidth(0, 1, "30px");
        layout.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_RIGHT);
    }

    protected boolean validate() {
        if (quickAddBox != null && quickAddBox.getText().trim().isEmpty()) {
            quickAddBox.setStyleName("input_box_error");
            return false;
        }
        return true;
    }

    public void setFocus(boolean focus) {
        quickAddBox.setFocus(focus);
    }

    public String getInputName() {
        return this.quickAddBox.getText();
    }

    public void setInputName(String text) {
        this.quickAddBox.setText(text);
    }

    public void addQuickAddKeyPressHandler(final KeyPressHandler handler) {
        if (quickAddBox == null)
            return;

        quickAddBox.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER)
                    return;

                if (!validate())
                    return;

                quickAddBox.setVisible(false);
                handler.onKeyPress(event);
            }
        });
    }

    public void addCancelHandler() {
        this.btnCancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                quickAddBox.setStyleName("input_box");
                QuickAddWidget.this.setVisible(false);
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        quickAddBox.setVisible(visible);
        quickAddBox.setFocus(visible);
    }
}
