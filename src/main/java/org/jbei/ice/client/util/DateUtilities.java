package org.jbei.ice.client.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class DateUtilities {

    public static String formatDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("EEE MMM d, y h:m a");
        return format.format(date);
    }
}
