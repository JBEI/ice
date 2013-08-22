package org.jbei.ice.client.bulkupload.sheet.cell;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.client.entry.display.model.AutoCompleteSuggestOracle;
import org.jbei.ice.lib.shared.dto.entry.AutoCompleteField;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sheet cell with auto complete values populated from the server
 *
 * @author Hector Plahar
 */
public class AutoCompleteSheetCell extends SheetCell {

    protected final SuggestBox box;
    protected final MultipleTextBox textBox;
    private int currentRow;

    public AutoCompleteSheetCell(AutoCompleteField field) {
        super();
        AutoCompleteSuggestOracle oracle = new AutoCompleteSuggestOracle(field);
        textBox = new MultipleTextBox();
        box = new SuggestBox(oracle, textBox);
        box.setStyleName("cell_input");

        textBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                String s = setDataForRow(currentRow);
                textBox.setBaseText(s);
            }
        });

        textBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                int code = event.getNativeKeyCode();
                if (KeyCodes.KEY_TAB != code && KeyCodes.KEY_ENTER != code)
                    return;

                textBox.setFocus(false);
            }
        });

        box.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                textBox.setFocus(true);
            }
        });
    }

    @Override
    public void setText(String text) {
        textBox.setBaseText(text);
    }

    @Override
    public String setDataForRow(int row) {
        String ret = textBox.getWholeText();
        SheetCellData data = new SheetCellData();
        data.setId(ret);
        data.setValue(ret);
        setWidgetValue(row, data);
        box.setText("");
        return ret;
    }

    @Override
    public void setFocus(int row) {
        textBox.setFocus(true);
        currentRow = row;
    }

    @Override
    public Widget getWidget(int row, boolean isCurrentSelection, int tabIndex) {
        box.setTabIndex(tabIndex);
        return box;
    }

    public boolean hasMultiSuggestions() {
        return true;
    }
}
