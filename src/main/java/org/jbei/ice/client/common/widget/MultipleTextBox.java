package org.jbei.ice.client.common.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.TextBoxBase;

public class MultipleTextBox extends TextBoxBase {
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
