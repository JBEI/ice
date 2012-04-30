package org.jbei.ice.client.common.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class PopupHandler implements ClickHandler {

    private final PopupPanel popup;
    private final int xOffset;
    private final int yOffset;

    public PopupHandler(Widget widget, Element autoHide, int xoffset, int yoffset) {
        this.popup = new PopupPanel();
        this.popup.setStyleName("add_to_popup");
        this.popup.setAutoHideEnabled(true);
        if (autoHide != null)
            this.popup.addAutoHidePartner(autoHide);
        this.popup.setWidget(widget);
        this.popup.setGlassEnabled(false);
        this.xOffset = xoffset;
        this.yOffset = yoffset;
    }

    @Override
    public void onClick(ClickEvent event) {
        if (!popup.isShowing()) {
            Widget source = (Widget) event.getSource();
            int x = source.getAbsoluteLeft() + xOffset;
            int y = source.getOffsetHeight() + source.getAbsoluteTop() + this.yOffset;
            popup.setPopupPosition(x, y);
            popup.show();
        } else {
            popup.hide();
        }
    }

    public void hidePopup() {
        if (this.popup == null || !this.popup.isShowing())
            return;

        this.popup.hide();
    }
}
