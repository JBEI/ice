package org.jbei.ice.client.collection.menu;

import java.util.Set;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 * Drop down list of user collections for the collections menu
 * 
 * @author Hector Plahar
 */
public class UserCollectionMultiSelect extends Composite { // TODO : split model and ui

    private final CellTable<FolderDetails> table;
    private final ListDataProvider<FolderDetails> dataProvider;
    private final MultiSelectionModel<FolderDetails> model;

    public UserCollectionMultiSelect(Button submitButton,
            ListDataProvider<FolderDetails> dataProvider) {

        VerticalPanel wrapper = new VerticalPanel();
        wrapper.addStyleName("background_white"); // TODO : generic style
        initWidget(wrapper);

        this.dataProvider = dataProvider;
        table = new CellTable<FolderDetails>();
        wrapper.add(table);

        wrapper.add(submitButton);
        wrapper.setCellHorizontalAlignment(submitButton, HasAlignment.ALIGN_RIGHT);

        this.dataProvider.addDataDisplay(table);

        model = new MultiSelectionModel<FolderDetails>();
        table.setSelectionModel(model,
            DefaultSelectionEventManager.<FolderDetails> createCheckboxManager());

        addSelectionColumn();
        addNameColumn();
    }

    public Set<FolderDetails> getSelected() {
        return model.getSelectedSet();
    }

    protected void addNameColumn() {
        TextColumn<FolderDetails> name = new TextColumn<FolderDetails>() {

            @Override
            public String getValue(FolderDetails object) {
                return object.getName();
            }
        };
        table.addColumn(name);
    }

    protected void addSelectionColumn() {
        final CheckboxCell columnCell = new CheckboxCell(true, false);

        table.addColumn(new Column<FolderDetails, Boolean>(columnCell) {

            @Override
            public Boolean getValue(FolderDetails object) {
                return model.isSelected(object);
            }
        });
    }
}
