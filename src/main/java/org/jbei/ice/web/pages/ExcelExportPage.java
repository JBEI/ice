package org.jbei.ice.web.pages;

import org.apache.commons.lang.StringUtils;

public abstract class ExcelExportPage extends ExportPage {
    @Override
    public String getMimeType() {
        return "application/vnd.ms-excel";
    }

    protected Object escapeValue(Object value) {
        if (value != null) {
            String stringValue = StringUtils.trim(value.toString());
            if (!StringUtils.containsNone(stringValue, new char[] { '\n', ',', '\t' })) {
                return "\"" + StringUtils.replace(stringValue, "\"", "\\\"") + "\"";
            }

            return stringValue;
        }

        return null;
    }
}
