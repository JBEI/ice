package org.jbei.ice.client.search.advanced;

import org.jbei.ice.client.collection.menu.UserCollectionMultiSelect;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class AddToMenu implements IsWidget {

    private final String LABEL = "Add To";
    private final Button button;

    public AddToMenu(UserCollectionMultiSelect addTo) {
        button = new Button(LABEL);
        MenuClickHandler addToHandler = new MenuClickHandler(addTo, button.getElement());
        button.addClickHandler(addToHandler);
    }

    @Override
    public Widget asWidget() {
        return button;
    }

    //
    // inner classes
    //

    private class MenuClickHandler implements ClickHandler {

        private final PopupPanel popup;

        public MenuClickHandler(Widget widget, Element autoHide) {
            this.popup = new PopupPanel();
            this.popup.setAutoHideEnabled(true);
            this.popup.addAutoHidePartner(autoHide);
            this.popup.setWidget(widget);
            this.popup.setGlassEnabled(true);
        }

        @Override
        public void onClick(ClickEvent event) {
            if (!popup.isShowing()) {
                Widget source = (Widget) event.getSource();
                int x = source.getAbsoluteLeft();
                int y = source.getOffsetHeight() + source.getAbsoluteTop();
                popup.setPopupPosition(x, y);
                popup.show();
            } else {
                popup.hide();
            }
        }

        public void hidePopup() { // see ExportAsMenu for usage
            if (this.popup == null || !this.popup.isShowing())
                return;

            this.popup.hide();
        }
    }
}
