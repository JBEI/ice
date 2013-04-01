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

    public PopupHandler(Widget widget, Element autoHide, boolean enableGlass) {
        this.popup = new PopupPanel();
        this.popup.setStyleName("add_to_popup");
        this.popup.setAutoHideEnabled(true);
        if (autoHide != null)
            this.popup.addAutoHidePartner(autoHide);
        this.popup.setWidget(widget);
        this.popup.setGlassEnabled(enableGlass);
    }

    public void setCloseHandler(CloseHandler<PopupPanel> handler) {
        if (closeHandlerRegistration != null)
            closeHandlerRegistration.removeHandler();
        closeHandlerRegistration = this.popup.addCloseHandler(handler);
    }

    public void addAutoHidePartner(Element autoHidePartner) {
        this.popup.addAutoHidePartner(autoHidePartner);
    }

    public void show() {
        popup.show();
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
}
