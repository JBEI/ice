package org.jbei.ice.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

public class Utils {
    public static void showWaitCursor(Element element) {

        if (element == null) {
            element = RootPanel.getBodyElement();
        }

        DOM.setStyleAttribute((com.google.gwt.user.client.Element) element, "cursor", "wait");
    }

    public static void showDefaultCursor(Element element) {

        if (element == null) {
            element = RootPanel.getBodyElement();
        }

        DOM.setStyleAttribute((com.google.gwt.user.client.Element) element, "cursor", "default");
    }

    public static void showProgressCursor() {

        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "progress");
    }

    public static void showNotAllowedCursor() {

        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "not-allowed");
    }

    public static void showPointerCursor(Element element) {

        if (element == null) {
            element = RootPanel.getBodyElement();
        }

        DOM.setStyleAttribute((com.google.gwt.user.client.Element) element, "cursor", "pointer");
    }
}
