package org.jbei.ice.client.profile.message;

import com.google.gwt.cell.client.Cell;
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
public class MessageSelectionColumnHeader extends CheckboxCell {
    private final MessageSelectionModel selectionModel;
    private MessageDataTable dataTable;

    public MessageSelectionColumnHeader(MessageDataTable table, MessageSelectionModel selectionModel,
            boolean dependsOnSelection, boolean handlesSelection) {
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
}
