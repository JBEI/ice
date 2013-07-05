package org.jbei.ice.client.entry.view.panel;

import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

/**
 * @author Hector Plahar
 */
public class EntrySequenceSelectionColumnHeader extends CheckboxCell {
    private final TableSelectionModel selectionModel;
    private final CellTable<SequenceAnalysisInfo> dataTable;

    public EntrySequenceSelectionColumnHeader(CellTable<SequenceAnalysisInfo> table,
            TableSelectionModel selectionModel, boolean dependsOnSelection, boolean handlesSelection) {
        super(dependsOnSelection, handlesSelection);
        this.selectionModel = selectionModel;
        this.dataTable = table;
    }

    @Override
    public void render(Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
        super.render(context, value, sb);
        String html;
        if (value) {
            html = "<sup>" + selectionModel.getSelectedSet().size() + "</sup>";
        } else
            html = "";
        sb.appendHtmlConstant(html);
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent, Boolean value,
            NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
        String type = event.getType();

        boolean enterPressed = "keydown".equals(type) && event.getKeyCode() == KeyCodes.KEY_ENTER;
        if ("change".equals(type) || enterPressed) {
            InputElement input = parent.getFirstChild().cast();
            Boolean isChecked = input.isChecked();

            if (isChecked) {
                selectionModel.setAllSelected(true);
                dataTable.redraw();
            } else {
                selectionModel.clear();
                selectionModel.setAllSelected(false);
            }
        }
    }

    public static class TableSelectionModel extends MultiSelectionModel<SequenceAnalysisInfo> {

        private boolean allSelected;

        public TableSelectionModel() {
            super(new ProvidesKey<SequenceAnalysisInfo>() {

                @Override
                public String getKey(SequenceAnalysisInfo item) {
                    return item.getFileId();
                }
            });
        }

        public void setAllSelected(boolean b) {
            allSelected = b;
        }

        public boolean isAllSelected() {
            return this.allSelected;
        }

        @Override
        public boolean isSelected(SequenceAnalysisInfo object) {
            if (allSelected) {
                setSelected(object, true);
            }

            return super.isSelected(object);
        }
    }
}
