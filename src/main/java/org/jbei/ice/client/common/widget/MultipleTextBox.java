package org.jbei.ice.client.common.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.TextBoxBase;

public class MultipleTextBox extends TextBoxBase {
    /**
     * Creates an empty multiple text box.
     */
    public MultipleTextBox() {
        super(Document.get().createTextInputElement());
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

    public String getWholeText() {
        return DOM.getElementProperty(getElement(), "value");
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
                    wholeString += ", ";
                }

                wholeString = wholeString + text; // + ", ";
                super.setText(wholeString);
            }
        }
    }
}
