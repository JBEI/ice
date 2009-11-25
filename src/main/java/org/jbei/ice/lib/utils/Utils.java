package org.jbei.ice.lib.utils;

import java.io.UnsupportedEncodingException;
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
    public static String join(String delimiter, Collection<?> s) {
        StringBuffer buffer = new StringBuffer();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
    
    /**
     * Convert byte array to Hex notation From a forum answer.
     * 
     * @param bytes
     * @return String of Hex representation
     * @throws UnsupportedEncodingException
     */
    public static String getHexString(byte[] bytes) throws UnsupportedEncodingException {
    	byte[] HEX_CHAR_TABLE = {
    		    (byte)'0', (byte)'1', (byte)'2', (byte)'3',
    		    (byte)'4', (byte)'5', (byte)'6', (byte)'7',
    		    (byte)'8', (byte)'9', (byte)'a', (byte)'b',
    		    (byte)'c', (byte)'d', (byte)'e', (byte)'f'
    		  };    
    	{
    	    byte[] hex = new byte[2 * bytes.length];
    	    int index = 0;

    	    for (byte b : bytes) {
    	      int v = b & 0xFF;
    	      hex[index++] = HEX_CHAR_TABLE[v >>> 4];
    	      hex[index++] = HEX_CHAR_TABLE[v & 0xF];
    	    }
    	    return new String(hex, "ASCII");
    	  }

    }

}
