package org.jbei.ice.client.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

public class Utils {
    public static void showWaitCursor(Element element) {
        if (element == null) {
            element = RootPanel.getBodyElement();
        }
        DOM.setStyleAttribute(element, "cursor", "wait");
    }

    public static void showDefaultCursor(Element element) {
        if (element == null) {
            element = RootPanel.getBodyElement();
        }
        DOM.setStyleAttribute(element, "cursor", "default");
    }

    public native boolean isValidEmail(String email) /*-{
        var reg1 = /(@.*@)|(\.\.)|(@\.)|(\.@)|(^\.)/; // not valid
        var reg2 = /^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z]{2,3}|[0-9]{1,3}) (\]?)$/; // valid
        return !reg1.test(email) && reg2.test(email);
    }-*/;
}
