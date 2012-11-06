package org.jbei.ice.client.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Utilities class for manipulating datetimes
 *
 * @author Hector Plahar
 */
public class DateUtilities {

    public static String formatDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("MMM dd, y h:mm a");
        return format.format(date);
    }

    public static String formatShorterDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy H:mm");
        return format.format(date);
    }

    public static String formatMediumDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("MMM dd, yyyy");
        return format.format(date);
    }
}
