package org.jbei.ice.lib.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class Utils {
	public final static LinkedHashSet<String> toHashSetFromCommaSeparatedString(String data) {
		
		LinkedHashSet<String> result = new LinkedHashSet<String> ();
		String[] temp = data.split(",");
		
		for (String i: temp) {
			i = i.trim();
			if (i.length() > 0) {
				result.add(i);
			}
		}
		
		return result;
		
	}
	
	/**
	 * Analogous to python's string.join method
	 * @param s Collection to work on
	 * @param delimiter String to use to join
	 * @return
	 */
    public static String join(String delimiter, Collection s) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

}
