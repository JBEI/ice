package org.jbei.ice.lib.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.SelectionMarker;

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
	
	public final static String toCommaSeparatedStringFromSelectionMarkers(Set<SelectionMarker> hashSet) {
		String result = null;
		ArrayList<String> temp = new ArrayList<String>();
		for (SelectionMarker selectionMarker : hashSet) {
			temp.add(selectionMarker.getName());
		}
		result = join(", ", temp);
		return result;
	}
	
	public final static String toCommaSeparatedStringFromLinks(Set<Link> hashSet) {
		String result = null;
		ArrayList<String> temp = new ArrayList<String>();
		for (Link link : hashSet) {
			temp.add(link.getLink());
		}
		result = join(", ", temp);
		return result;
	}
	
	public final static String toCommaSeparatedStringFromNames(Set<Name> hashSet) {
		String result = null;
		ArrayList<String> temp = new ArrayList<String>();
		for (Name name: hashSet) {
			temp.add(name.getName());
		}
		result = join(", ", temp);
		return result;
	}
	
	public final static String toCommaSeparatedStringFromPartNumbers(Set<PartNumber> hashSet) {
		String result = null;
		ArrayList<String> temp = new ArrayList<String>();
		for (PartNumber partNumber: hashSet) {
			temp.add(partNumber.getPartNumber());
		}
		result = join(", ", temp);

		return result;
	}
	
	public final static String toCommaSeparatedStringFromEntryFundingSources(Set<EntryFundingSource> hashSet) {
		String result = null;
		ArrayList<String> temp = new ArrayList<String>();
		for (EntryFundingSource entryFundingSource: hashSet) {
			FundingSource fundingSource = entryFundingSource.getFundingSource();
			temp.add(fundingSource.getFundingSource());
			temp.add(fundingSource.getPrincipalInvestigator());
		}
		result = join(", ", temp);
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
        	Object item = iter.next();
        	if (item != null) {
        		buffer.append(item);
	        	if (iter.hasNext()) {
	                buffer.append(delimiter);
	            }
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
