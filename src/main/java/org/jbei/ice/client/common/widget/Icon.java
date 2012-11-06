package org.jbei.ice.client.common.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Icon widget for using font awesome with event handling
 *
 * @author Hector Plahar
 */
public class Icon extends Widget implements HasClickHandlers {

    public Icon(FAIconType type) {
        setElement(DOM.createElement("i"));
        this.setStyleName(type.getStyleName());
        this.addStyleName("font-awesome"); // chrome sometimes has issues with the fact that the i-element is empty
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    public void setType(FAIconType type) {
        this.setStyleName(type.getStyleName());
        this.addStyleName("font-awesome"); // chrome sometimes has issues with the fact that the i-element is empty
    }
}
