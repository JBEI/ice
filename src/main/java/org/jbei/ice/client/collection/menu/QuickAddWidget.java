package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Hector Plahar
 */
public class QuickAddWidget extends Composite implements QuickAddWidgetPresenter.View {

    private final TextBox quickAddBox;
    private final QuickAddWidgetPresenter presenter;
    private final FlexTable layout;
    private final Icon iconSave;
    private final Icon iconCancel;

    public QuickAddWidget() {
        quickAddBox = new TextBox();
        quickAddBox.setStyleName("input_box");
        quickAddBox.addStyleName("pad-4");
        quickAddBox.getElement().setAttribute("placeholder", "Enter collection name");
        quickAddBox.setWidth("170px");
        quickAddBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                quickAddBox.setText("");
            }
        });

        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        initWidget(layout);

        iconCancel = new Icon(FAIconType.REMOVE_SIGN);
        iconCancel.addStyleName("remove_filter");
        iconSave = new Icon(FAIconType.OK_SIGN);
        iconSave.addStyleName("add_filter_style");

        layout.setWidget(0, 0, quickAddBox);

        layout.setWidget(0, 1, iconSave);
        layout.getFlexCellFormatter().setWidth(0, 1, "20px");
        layout.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_CENTER);

        layout.setWidget(0, 2, iconCancel);
        layout.getFlexCellFormatter().setWidth(0, 2, "20px");
        layout.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasAlignment.ALIGN_CENTER);

        presenter = new QuickAddWidgetPresenter(this);
    }

    // todo : move to model/presenter/handler
    protected boolean validate() {
        if (quickAddBox != null && quickAddBox.getText().trim().isEmpty()) {
            quickAddBox.setStyleName("entry_input_error");
            return false;
        }
        return true;
    }

    public String getInputName() {
        return this.quickAddBox.getText();
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

    public void addSubmitHandler(ClickHandler handler) {
        this.iconSave.addClickHandler(handler);
    }

    @Override
    public void addCancelHandler(ClickHandler handler) {
        this.iconCancel.addClickHandler(handler);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        quickAddBox.setVisible(visible);
        quickAddBox.setFocus(visible);
    }
}
