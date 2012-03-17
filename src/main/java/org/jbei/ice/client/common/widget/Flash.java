package org.jbei.ice.client.common.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for display flash objects. TODO : currently sequence vector editor
 * 
 * @author Hector Plahar
 * 
 */
public class Flash extends Widget {

    public static String SWF_LOCATION = "static/swf/";

    public Flash(Parameters params) {
        setElement(DOM.createDiv());
        DOM.setElementAttribute(getElement(), "style", "height: 100%");
        Element element = getFlashElement(params);
        DOM.appendChild(getElement(), element);
    }

    public Element getFlashElement(Parameters params) {
        String url = GWT.getHostPageBaseURL();
        Element el = DOM.createElement("EMBED");
        DOM.setElementAttribute(el, "src", url + SWF_LOCATION + params.getSwfPath() + "?entryId="
                + params.getEntryId() + "&sessionId=" + params.getSessiondId());
        DOM.setElementAttribute(el, "width", "100%");
        DOM.setElementAttribute(el, "height", "100%");
        DOM.setElementPropertyBoolean(el, "play", true);
        DOM.setElementAttribute(el, "wmode", "transparent");
        DOM.setElementPropertyBoolean(el, "loop", false);
        DOM.setElementAttribute(el, "quality", "high");
        DOM.setElementAttribute(el, "bgcolor", "#869ca7");
        DOM.setElementAttribute(el, "align", "middle");
        DOM.setElementAttribute(el, "pluginspage", "http://www.adobe.com/go/getflashplayer");
        return el;
    }

    public static class Parameters {
        private String sessiondId;
        private String entryId;
        private String swf;

        public String getSessiondId() {
            return sessiondId;
        }

        public void setSessiondId(String sessiondId) {
            this.sessiondId = sessiondId;
        }

        public String getEntryId() {
            return entryId;
        }

        public void setEntryId(String entryId) {
            this.entryId = entryId;
        }

        public String getSwfPath() {
            return this.swf;
        }

        public void setSwfPath(String swf) {
            this.swf = swf;
        }
    }
}
