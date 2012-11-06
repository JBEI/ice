package org.jbei.ice.client.common.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class PopupHandler implements ClickHandler {

    private final PopupPanel popup;
    private HandlerRegistration closeHandlerRegistration;
    private final Widget widget;

    public PopupHandler(Widget widget, Element autoHide, boolean enableGlass) {
        this.popup = new PopupPanel();
        this.popup.setStyleName("add_to_popup");
        this.popup.setAutoHideEnabled(true);
        if (autoHide != null)
            this.popup.addAutoHidePartner(autoHide);
        this.popup.setWidget(widget);
        this.popup.setGlassEnabled(enableGlass);
        this.widget = widget;
    }

    public void setCloseHandler(CloseHandler<PopupPanel> handler) {
        if (closeHandlerRegistration != null)
            closeHandlerRegistration.removeHandler();
        closeHandlerRegistration = this.popup.addCloseHandler(handler);
    }

    @Override
    public void onClick(ClickEvent event) {
        if (!popup.isShowing()) {
            popup.showRelativeTo((Widget) event.getSource());
        } else {
            popup.hide();
        }
    }

    public boolean popupIsShowing() {
        return popup.isShowing();
    }

    public void hidePopup() {
        if (this.popup == null || !this.popup.isShowing())
            return;

        this.popup.hide();
    }

    @Deprecated
    public void showPopup() {
        if (this.popup == null || this.popup.isShowing())
            return;

        this.popup.showRelativeTo(widget);
    }
}
