package org.jbei.ice.client.bulkimport.sheet;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import org.jbei.ice.client.common.widget.MultipleTextBox;

/**
 * @author Hector Plahar
 */
public class MultiSuggestSheetCell extends SheetCell {

    private final MultiWordSuggestOracle oracle;
    private final SuggestBox box;
    private final MultipleTextBox textBox;

    public MultiSuggestSheetCell() {
        super();

        oracle = new MultiWordSuggestOracle();
        textBox = new MultipleTextBox();
//        textBox.setWidth("129px");
        box = new SuggestBox(oracle, textBox);
        box.setStyleName("cell_input");

        initWidget(box);
    }

    @Override
    public void setText(String text) {
        box.setText(text);
    }

    @Override
    public String getWidgetText() {
        String ret = textBox.getWholeText();
        box.setText("");
        return ret;
    }

    @Override
    public void setFocus() {
        textBox.setFocus(true);
    }

    /**
     * Adds the suggestions that will be presented to user to oracle
     *
     * @param data list of strings presented to user
     */
    public void addOracleData(ArrayList<String> data) {
        oracle.clear();
        oracle.addAll(data);
    }

    public boolean hasMultiSuggestions() {
        return true;
    }
}
