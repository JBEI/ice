package org.jbei.ice.web.forms;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.utils.Utils;

/**
 * Way to map comma separated text field to set of items of class type
 * TODO: Wicket has a nice dynamic getter/setter generation from field name.
 * Implement something similar. Right now, it must be passed the method names
 * 
 * @author tham
 * @param <T>
 *
 */
public class CommaSeparatedField<T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	protected ArrayList<T> items = null; 
	protected Class klass = null;
	protected String getterName = null;
	protected String setterName= null;
	
	public CommaSeparatedField(Class c, String getterName, String setterName) {
		items = new ArrayList<T> ();
		klass = c;
		this.getterName = getterName;
		this.setterName = setterName;
	}

	public CommaSeparatedField(Class k, Collection c, String getterName, String setterName) {
		items = new ArrayList<T> (c);
		klass = k;
		this.getterName = getterName;
		this.setterName = setterName;
	}
	
	/**
	 * Get a comma separated string from set of objects
	 * @return Comma separated string
	 * @throws FormException
	 */
	public String getString() throws FormException {
		ArrayList<String> itemsAsString = new ArrayList<String> ();
		
		for (T item : items) {
			
			try {
				Method getMethod = item.getClass().getDeclaredMethod(getterName, 
						new Class[] {});
				itemsAsString.add((String) getMethod.invoke(item,(Object[]) null));
				
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new FormException("Couldn't use getter in form");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new FormException("Couldn't use getter in form");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new FormException("Couldn't invoke getter in form");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new FormException("Couldn't invoke getter in form");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new FormException("Couldn't invoke getter in form");
			}
			
		}
		
		String result = Utils.join(", ", itemsAsString);
		return result;	
		}
		
	/**
	 * Generates an ArrayList of objects of type created from input string
	 * @param Input comma separated string
	 * @return ArrayList of objects with its property set
	 * @throws FormException
	 */
	public ArrayList<T> setString(String string) throws FormException {
		ArrayList<T> result = new ArrayList<T> ();
		if (string == null) {
			return result;
		}
		
		String[] itemsAsString = string.split("\\s*,+\\s*");
		
		for (int i=0; i < itemsAsString.length; i++) {
			String currentItem = itemsAsString[i];
			
			try {
				T instance = (T) klass.newInstance();
				Method setMethod = klass.getDeclaredMethod(setterName, new Class[] {String.class});
				setMethod.invoke(instance, currentItem);
				result.add(instance);
				
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new FormException("Couldn't instantiate class");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new FormException("Couldn't instantiate class");
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new FormException("Couldn't get setter in form");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new FormException("Couldn't get setter in form");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new FormException("Couldn't invoke setter in form");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new FormException("Couldn't invoke setter in form");
			}
		}
		items = result;
		return result;
	}
	
	public HashSet<T> getItemsAsSet() {
		HashSet<T> result = new HashSet<T>(items);
		return result;
	}
	
	public static void main(String[] args) {
		//Names
		CommaSeparatedField<Name> csf = 
			new CommaSeparatedField<Name>(Name.class, "getName", "setName");
		ArrayList<Name> result = null;
		try {
			result = csf.setString("this, is,  a, different,name");
		} catch (FormException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(result.size());
		for (Name item : result) {
			System.out.println(item.toString() + ":" + item.getName());
			
		}
		
		csf = new CommaSeparatedField<Name>(Name.class, result, "getName", "setName");
		try {
			System.out.println(csf.getString());
		} catch (FormException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//PartNumbers
		CommaSeparatedField<PartNumber> csf2 = 
			new CommaSeparatedField<PartNumber>(PartNumber.class, "getPartNumber", "setPartNumber");
		ArrayList<PartNumber> result2 = null;
		try {
			result2 = csf2.setString("this, is,  a, different,part number");
		} catch (FormException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(result2.size());
		for (PartNumber item : result2) {
			System.out.println(item.toString() + ":" + item.getPartNumber());
			
		}
		
		csf2 = new CommaSeparatedField<PartNumber>(PartNumber.class, result2,  "getPartNumber", "setPartNumber");
		try {
			System.out.println(csf2.getString());
		} catch (FormException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
