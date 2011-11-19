package org.jbei.ice.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

public class Utils {
    public static void showWaitCursor(Element element) {

        if (element == null) {
            GWT.log("element was null");
            element = RootPanel.getBodyElement();
        }

        DOM.setStyleAttribute(element, "cursor", "wait");
    }

    public static void showDefaultCursor(Element element) {

        if (element == null) {
            GWT.log("element was null");
            element = RootPanel.getBodyElement();
        }

        DOM.setStyleAttribute(element, "cursor", "default");
    }

    /**
     * splits a string around commas. TODO : current version is the quick and dirty one
     * 
     * @param line
     * @return
     */
    public ArrayList<String> splitComma(String line) {

        List<String> split = Arrays.asList(line.split(","));
        ArrayList<String> result = new ArrayList<String>(split);
        return result;
    }

    //
    //    public static void showProgressCursor() {
    //
    //        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "progress");
    //    }
    //
    //    public static void showNotAllowedCursor() {
    //        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "not-allowed");
    //    }
    //
    //    public static void showPointerCursor(Element element) {
    //
    //        if (element == null) {
    //            element = RootPanel.getBodyElement();
    //        }
    //
    //        DOM.setStyleAttribute(element, "cursor", "pointer");
    //    }
    //
    //    public static void showEResizeCursor(Element element) {
    //        DOM.setStyleAttribute(element, "cursor", "e-resize");
    //    }
}
