package org.jbei.ice.client.common.table.cell;

import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.client.common.table.EntrySelectionModel;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Header cell for the selection column in entry tables
 *
 * @author Hector Plahar
 */
public class EntrySelectionColumnHeaderCell<T extends EntryInfo> extends CheckboxCell {

    private final EntrySelectionModel<T> selectionModel;
    private final EntryDataTable<T> tEntryDataTable;

    public EntrySelectionColumnHeaderCell(EntryDataTable<T> tEntryDataTable, EntrySelectionModel<T> selectionModel,
            boolean dependsOnSelection, boolean handlesSelection) {
        super(dependsOnSelection, handlesSelection);
        this.selectionModel = selectionModel;
        this.tEntryDataTable = tEntryDataTable;
    }

    @Override
    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        super.render(context, value, sb);
        String html;
        if (value) {
            html = "<sup>" + selectionModel.getSelectedSet().size() + "</sup>";
        } else
            html = "";
        sb.appendHtmlConstant(html);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, Boolean value,
            NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
        String type = event.getType();

        boolean enterPressed = "keydown".equals(type) && event.getKeyCode() == KeyCodes.KEY_ENTER;
        if ("change".equals(type) || enterPressed) {
            InputElement input = parent.getFirstChild().cast();
            Boolean isChecked = input.isChecked();

            if (isChecked) {
                selectionModel.setAllSelected(true);
                tEntryDataTable.redraw();
            } else {
                selectionModel.clear();
                selectionModel.setAllSelected(false);
            }
        }
    }
}
