package org.jbei.ice.client.common.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Icon widget for using font awesome with event handling
 *
 * @author Hector Plahar
 */
public class Icon extends Widget {

    public Icon(FAIconType type) {
        setElement(DOM.createElement("i"));
        this.setStyleName(type.getStyleName());
    }
}
