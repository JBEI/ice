package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.user.client.ui.Composite;

/**
 * @author Hector Plahar
 */
public abstract class SheetCell extends Composite {

    public SheetCell() {
    }

    /**
     * Set text value of input widget; typically text box base. This is used when focus shifts from displaying the label
     * to the actual widget. This gives the widget the chance to set their text (which is the existing)
     *
     * @param text value to set
     */
    public abstract void setText(String text);

    /**
     * @return the text value of the input widget
     */
    public abstract String getWidgetText();

    /**
     * Give focus to the input widget
     */
    public abstract void setFocus();

    /**
     * This is meant to be overriden in subclasses that have input boxes with multi suggestions
     *
     * @return true if there are multiple suggestions that are presented to the user on input, false otherwise
     */
    public boolean hasMultiSuggestions() {
        return false;
    }
}
