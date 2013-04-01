package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.bulkupload.sheet.CellWidgetCallback;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * @author Hector Plahar
 */
public class CellWidget extends Composite {

    private final FocusPanel panel;
    private final HTML widget;
    private final HTML corner;
    private final int tabIndex;
    private String display;
    private final String value;
    private int row;
    private int index;

    public CellWidget(String value, int row, int col, int size) {
        this(value, (row * size) + col + 1);
        this.row = row;
        this.index = col;
    }

    public CellWidget(String value, int tabIndex) {
        widget = new HTML();
        corner = new HTML("");
        corner.setVisible(false);
        HTMLPanel htmlPanel = new HTMLPanel("<span id=\"cell_widget_" + tabIndex + "\"></span><span id=\"cell_widget_"
                                                    + tabIndex + "_corner\"></span>");
        htmlPanel.add(widget, ("cell_widget_" + tabIndex));
        htmlPanel.add(corner, "cell_widget_" + tabIndex + "_corner");
        panel = new FocusPanel(htmlPanel);
        initWidget(panel);

        setValue(value);
        this.value = value;
        this.tabIndex = tabIndex;
        panel.setTabIndex(tabIndex);
        panel.setStyleName("cell_border");

        addFocusHandler();
    }

    public void setValue(String value) {
        display = value;
        String title = value;

        if (value.length() > 19)
            display = (value.substring(0, 16) + "...");

        widget.setTitle(title);
        widget.setText(display);
        widget.setStyleName("cell");
        corner.setHTML("<div style=\"position: relative; width: 5px; height: 5px; background-color: "
                               + "#0082C0; top: -5px; right: -124px; border: 2px solid white; cursor: crosshair\">"
                               + "</div>");
    }

    private void addFocusHandler() {
        panel.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                widget.setStyleName("cell_focus");
                corner.setVisible(false); // tODO change to true
            }
        });

        panel.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                widget.setStyleName("cell"); // todo : if focus is switch to another application (e.g. IDE)
                // TODO : this is called also. need to check if focus is on another cell
                widget.setText(display);
                corner.setVisible(false);
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

    public void hideCorner() {
        corner.setVisible(false);
    }

    public int getTabIndex() {
        return this.tabIndex;
    }

    public void addWidgetCallback(final CellWidgetCallback callback) {
        corner.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                callback.onMouseDown(row, index);
            }
        });

        this.addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                callback.onMouseUp(row, index);
            }
        }, MouseUpEvent.getType());


        this.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                callback.onMouseOver(row, index);
            }
        }, MouseOverEvent.getType());
    }

    public String getValue() {
        return this.value;
    }
}
