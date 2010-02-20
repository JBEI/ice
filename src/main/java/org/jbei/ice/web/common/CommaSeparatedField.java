package org.jbei.ice.web.common;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.forms.FormException;

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
    protected Class<T> klass = null;
    protected String getterName = null;
    protected String setterName = null;

    public CommaSeparatedField(Class<T> c, String getterName, String setterName) {
        items = new ArrayList<T>();
        klass = c;
        this.getterName = getterName;
        this.setterName = setterName;
    }

    @SuppressWarnings("unchecked")
    public CommaSeparatedField(Class<T> k, Collection<Object> c, String getterName,
            String setterName) {
        items = new ArrayList(c);
        klass = k;
        this.getterName = getterName;
        this.setterName = setterName;
    }

    /**
     * Get a comma separated string from set of objects
     * 
     * @return Comma separated string
     * @throws FormException
     */
    public String getString() throws FormException {
        ArrayList<String> itemsAsString = new ArrayList<String>();

        for (T item : items) {

            try {
                Method getMethod = item.getClass().getDeclaredMethod(getterName, new Class[] {});
                itemsAsString.add((String) getMethod.invoke(item, (Object[]) null));

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
     * 
     * @param Input
     *            comma separated string
     * @return ArrayList of objects with its property set
     * @throws FormException
     */
    public ArrayList<T> setString(String string) throws FormException {
        ArrayList<T> result = new ArrayList<T>();
        if (string == null) {
            return result;
        }

        String[] itemsAsString = string.split("\\s*,+\\s*");

        for (int i = 0; i < itemsAsString.length; i++) {
            String currentItem = itemsAsString[i];

            try {
                T instance = klass.newInstance();
                Method setMethod = klass
                        .getDeclaredMethod(setterName, new Class[] { String.class });
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

    }
}
