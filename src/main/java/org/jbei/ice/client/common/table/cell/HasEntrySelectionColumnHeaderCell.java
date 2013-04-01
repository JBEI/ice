package org.jbei.ice.client.common.table.cell;

import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.client.common.table.HasEntrySelectionModel;
import org.jbei.ice.shared.dto.entry.HasEntryInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Hector Plahar
 */
public class HasEntrySelectionColumnHeaderCell<T extends HasEntryInfo> extends CheckboxCell {

    private final HasEntrySelectionModel<T> selectionModel;
    private final HasEntryDataTable<T> tEntryDataTable;

    public HasEntrySelectionColumnHeaderCell(HasEntryDataTable<T> tEntryDataTable,
            HasEntrySelectionModel<T> selectionModel, boolean dependsOnSelection, boolean handlesSelection) {
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

        boolean enterPressed = "keydown".equals(type)
                && event.getKeyCode() == KeyCodes.KEY_ENTER;
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
