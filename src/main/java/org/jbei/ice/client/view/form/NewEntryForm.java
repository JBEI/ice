package org.jbei.ice.client.view.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Parent class for forms used to create new entries
 * 
 * @author Hector Plahar
 */
public abstract class NewEntryForm extends Composite {

    protected final FlexTable layout;
    protected final HashMap<AutoCompleteField, ArrayList<String>> data;

    public NewEntryForm(HashMap<AutoCompleteField, ArrayList<String>> data) {
        layout = new FlexTable();
        this.data = data;
    }

    public Widget createTextFieldWithHelpText(int width, String helpText) {

        HorizontalPanel panel = new HorizontalPanel();
        TextBox input = new TextBox();
        input.setStyleName("inputbox");
        input.setWidth(String.valueOf(width));
        panel.add(input);
        Label help = new Label(helpText);
        help.setStyleName("help_text");
        panel.add(help);
        return panel;
    }

    public Widget createWidgetWithHelpText(Widget widget, String helpText, boolean horizontal) {

        CellPanel panel;
        if (horizontal)
            panel = new HorizontalPanel();
        else
            panel = new VerticalPanel();

        panel.add(widget);
        Label help = new Label(helpText);
        help.setStyleName("help_text");
        panel.add(help);

        return panel;
    }

    /**
     * @return text input for autocomplete data
     */
    private SuggestBox createSuggestBox(TreeSet<String> data) {

        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
        oracle.addAll(data);
        SuggestBox box = new SuggestBox(oracle, new MultipleTextBox());
        box.setStyleName("inputbox");
        return box;
    }

    public Widget createAutoCompleteForPromoters(String width) {

        SuggestBox box = createSuggestBox(new TreeSet<String>(data.get(AutoCompleteField.PROMOTER)));
        box.setWidth(width);
        return box;
    }

    public SuggestBox createAutoCompleteForSelectionMarkers(String width) {

        SuggestBox box = this.createSuggestBox(new TreeSet<String>(data
                .get(AutoCompleteField.SELECTION_MARKER)));
        box.setWidth(width);
        return box;
    }

    public Widget createAutoCompleteForPlasmidNames(String width) {

        SuggestBox box = this.createSuggestBox(new TreeSet<String>(data
                .get(AutoCompleteField.PLASMID_NAME)));
        box.setWidth(width);
        return box;
    }

    public Widget createAutoCompleteForOriginOfReplication(String width) {

        SuggestBox box = this.createSuggestBox(new TreeSet<String>(data
                .get(AutoCompleteField.ORIGIN_OF_REPLICATION)));
        box.setWidth(width);
        return box;
    }

    //
    // Abstract Methods
    // 

    /**
     * Form initialization to layout the required components
     */
    protected abstract void init(Button saveButton);

    /**
     * @return user entered data wrapped up in an entry object
     */
    public abstract EntryInfo getEntry();

    //
    // inner classes
    // 

    private class MultipleTextBox extends TextBoxBase {
        /**
         * Creates an empty multiple text box.
         */
        public MultipleTextBox() {
            this(Document.get().createTextInputElement(), "gwt-TextBox");
        }

        /**
         * This constructor may be used by subclasses to explicitly use an existing
         * element. This element must be an <input> element whose type is
         * 'text'.
         * 
         * @param element
         *            the element to be used
         */
        protected MultipleTextBox(Element element) {
            super(element);
            assert InputElement.as(element).getType().equalsIgnoreCase("text");
        }

        MultipleTextBox(Element element, String styleName) {
            super(element);
            if (styleName != null) {
                setStyleName(styleName);
            }
        }

        @Override
        public String getText() {
            String wholeString = super.getText();
            String lastString = wholeString;
            if (wholeString != null && !wholeString.trim().equals("")) {
                int lastComma = wholeString.trim().lastIndexOf(",");
                if (lastComma > 0) {
                    lastString = wholeString.trim().substring(lastComma + 1);
                }
            }
            return lastString;
        }

        @Override
        public void setText(String text) {
            String wholeString = super.getText();
            if (text != null && text.equals("")) {
                super.setText(text);
            } else {

                if (wholeString != null) {
                    int lastComma = wholeString.trim().lastIndexOf(",");
                    if (lastComma > 0) {
                        wholeString = wholeString.trim().substring(0, lastComma);
                    } else {
                        wholeString = "";
                    }

                    if (!wholeString.trim().endsWith(",") && !wholeString.trim().equals("")) {
                        wholeString = wholeString + ", ";
                    }

                    wholeString = wholeString + text; // + ", ";
                    super.setText(wholeString);
                }
            }
        }
    }
}
