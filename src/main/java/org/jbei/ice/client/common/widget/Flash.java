package org.jbei.ice.client.common.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for display flash objects. TODO : currently sequence vector editor
 * 
 * @author Hector Plahar
 * 
 */
public class Flash implements IsWidget {

    public static String SWF_LOCATION = "static/swf/";
    private final HTML widget;

    public Flash(Parameters params) {
        String url = GWT.getHostPageBaseURL();
        String html = "<object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540002\" id=\"VectorEditor\" width=\"100%\" height=\"100%\" codebase=\"https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab\"> "
                + "<param name=\"movie\" value=\"VectorEditor.swf\">" // TODO : this goes along with swfPath
                + "<param name=\"quality\" value=\"high\">"
                + "<param name=\"bgcolor\" value=\"#869ca7\">"
                + "<param name=\"wmode\" value=\"transparency\">"
                + "<param name=\"allowScriptAccess\" value=\"sameDomain\">"
                + "<embed src=\""
                + url
                + "static/swf/"
                + params.getSwfPath()
                + "?entryId="
                + params.getEntryId()
                + "&amp;sessionId="
                + params.getSessiondId()
                + "\" quality=\"high\" bgcolor=\"#869ca7\" width=\"100%\" height=\"100%\" name=\"VectorEditor\" align=\"middle\" play=\"true\" loop=\"false\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.adobe.com/go/getflashplayer\"></object>";
        widget = new HTML(html);
        widget.setHeight("100%");
        widget.setWidth("100%");
    }

    @Override
    public Widget asWidget() {
        return widget;
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
