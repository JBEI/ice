package org.jbei.ice.entry;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Hector Plahar
 */
public class PartDataUtil {

    /**
     * Concatenate a Collection of Strings using the given delimiter.
     */
    public static String join(Collection<?> s) {
        if (s == null)
            return "";
        StringBuilder buffer = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item != null) {
                buffer.append(item);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }

        }
        return buffer.toString();
    }
}
