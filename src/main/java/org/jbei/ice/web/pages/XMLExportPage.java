package org.jbei.ice.web.pages;

import org.apache.commons.lang.StringEscapeUtils;

public abstract class XMLExportPage extends ExportPage {
    @Override
    public String getMimeType() {
        return "text/xml";
    }

    protected String getHeader() {
        return "<?xml version=\"1.0\"?>";
    }

    protected Object escapeXMLValue(Object value) {
        if (value != null) {
            return StringEscapeUtils.escapeXml(value.toString());
        }

        return "";
    }
}
