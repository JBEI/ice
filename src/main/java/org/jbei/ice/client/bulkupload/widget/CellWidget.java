package org.jbei.ice.client.bulkupload.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Hector Plahar
 */
public class CellWidget extends Composite {

    private final FocusPanel panel;
    private final HTML widget;
    private final int tabIndex;

    public CellWidget(String value, int row, int col, int size) {
        this(value, (row * size) + col + 1);
    }

    public CellWidget(String value, int tabIndex) {
        widget = new HTML();
        panel = new FocusPanel(widget);
        initWidget(panel);

        setValue(value);
        this.tabIndex = tabIndex;
        panel.setTabIndex(tabIndex);
        panel.setStyleName("cell_border");

        addFocusHandler();
    }

    public void setValue(String value) {

        String display = value;
        String title = value;

        if (value.length() > 19)
            display = (value.substring(0, 16) + "...");

        widget.setTitle(title);
        widget.setText(display);
        widget.setStyleName("cell");
    }

    private void addFocusHandler() {
        panel.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                widget.setStyleName("cell_focus");
            }
        });

        panel.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                widget.setStyleName("cell");
            }
        });
    }

    public void showError(String errMsg) {
        widget.addStyleName("cell_error");
        widget.setTitle(errMsg);
    }

    public void clearError() {
        widget.removeStyleName("cell_error");
        widget.setTitle("");
    }

    public HTML getLabel() {
        return this.widget;
    }

    public void setFocus(boolean focus) {
        panel.setFocus(focus);
    }

    public int getTabIndex() {
        return this.tabIndex;
    }
}
