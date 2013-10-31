package org.jbei.ice.client.bulkupload.widget;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 * Widget that allows user to select the global readable permissions for
 * groups
 *
 * @author Hector Plahar
 */
public class PermissionsSelection implements IsWidget {

    private final FocusPanel parent;
    private final CellTable<UserGroup> table;
    private final MultiSelectionModel<UserGroup> model;
    private final ListDataProvider<UserGroup> dataProvider;

    interface SelectionResource extends CellTable.Resources {

        static SelectionResource INSTANCE = GWT.create(SelectionResource.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/PermissionSelection.css")
        CellTable.Style cellTableStyle();
    }

    public PermissionsSelection() {
        Icon icon = new Icon(FAIconType.SHIELD);
        icon.setTitle("Click to set permissions");
        icon.addStyleName("display-inline");
        icon.removeStyleName("font-awesome");

        HTMLPanel panel = new HTMLPanel("<span id=\"permission_icon\"></span> Permissions <i class=\""
                                                + FAIconType.CARET_DOWN.getStyleName() + "\"></i>");
        panel.add(icon, "permission_icon");
        panel.setStyleName("display-inline");
        panel.setTitle("Set read permissions for entries");

        parent = new FocusPanel(panel);
        parent.setStyleName("bulk_upload_visibility");
        parent.addStyleName("opacity_hover");

        model = new MultiSelectionModel<UserGroup>();

        table = new CellTable<UserGroup>(30, SelectionResource.INSTANCE);
        addSelectionColumn();
        addNameColumn();
        table.setEmptyTableWidget(new HTML("<i class=\"font-75em\">No groups available.</i>"));
        table.setSelectionModel(model, DefaultSelectionEventManager.<UserGroup>createCheckboxManager());

        table.addCellPreviewHandler(new CellPreviewEvent.Handler<UserGroup>() {

            @Override
            public void onCellPreview(CellPreviewEvent<UserGroup> event) {
                boolean clicked = "click".equals(event.getNativeEvent().getType());
                if (!clicked || event.getColumn() == 0)
                    return;

                UserGroup selected = event.getValue();
                boolean select = model.isSelected(selected);
                model.setSelected(selected, !select);
                // we can either trigger a submit when user clicks a single cell
                // or has the check box selected only (user then has to click submit)
                // currently choosing the latter option dispatchSubmitEvent();
            }
        });

        dataProvider = new ListDataProvider<UserGroup>();
        dataProvider.addDataDisplay(table);

        PopupHandler addToHandler = new PopupHandler(table, parent.getElement(), false);
        parent.addClickHandler(addToHandler);
    }

    public void setData(ArrayList<UserGroup> data) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(data);

        for (UserGroup datum : data) {
            // excluding everyone group which may be set to public
            if (datum.getType() != GroupType.PUBLIC
                    || datum.getUuid().equalsIgnoreCase("8746a64b-abd5-4838-a332-02c356bbeac0"))
                continue;

            model.setSelected(datum, true);
        }
    }

    public Set<UserGroup> getSelectedGroups() {
        return model.getSelectedSet();
    }

    /**
     * Uses the id in option select to enable the check boxes for those that exist in the data provider
     *
     * @param data data that needs to be enabled/checked
     */
    public void setEnabled(ArrayList<OptionSelect> data) {
        for (UserGroup optionSelect : dataProvider.getList()) {
            for (OptionSelect aData : data) {
                if (optionSelect.getId() == aData.getId()) {
                    model.setSelected(optionSelect, true);
                    break;
                }
            }
        }
    }

    @Override
    public Widget asWidget() {
        return parent;
    }

    protected void addSelectionColumn() {
        final CheckboxCell columnCell = new CheckboxCell(true, false);

        Column<UserGroup, Boolean> selectionCol = new Column<UserGroup, Boolean>(columnCell) {

            @Override
            public Boolean getValue(UserGroup object) {
                return model.isSelected(object);
            }
        };

        table.addColumn(selectionCol);
        table.setColumnWidth(selectionCol, "5px");
    }

    protected void addNameColumn() {
        TextColumn<UserGroup> name = new TextColumn<UserGroup>() {

            @Override
            public String getValue(UserGroup object) {
                return object.getLabel();
            }
        };
        table.addColumn(name);
        table.setColumnWidth(name, "200px");
    }
}
