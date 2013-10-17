package org.jbei.ice.client.profile.entry;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * View display of the user's genetic parts
 *
 * @author Hector Plahar
 */
public class ProfilePartsView extends Composite implements IUserProfilePanel {

    private CollectionDataTable table;
    private final VerticalPanel panel;

    public ProfilePartsView(ServiceDelegate<PartData> delegate) {

        this.table = new CollectionDataTable(delegate) {
            @Override
            protected ArrayList<DataTableColumn<PartData, ?>> createColumns(ServiceDelegate<PartData> delegate) {
                ArrayList<DataTableColumn<PartData, ?>> columns = new ArrayList<DataTableColumn<PartData, ?>>();
                columns.add(super.addTypeColumn(true, 60, com.google.gwt.dom.client.Style.Unit.PX));
                DataTableColumn<PartData, PartData> partIdCol = addPartIdColumn(delegate, false, 120, com.google.gwt.dom
                        .client.Style.Unit.PX);
                columns.add(partIdCol);
                columns.add(super.addNameColumn(120, com.google.gwt.dom.client.Style.Unit.PX, true));
                columns.add(super.addSummaryColumn());
                columns.add(super.addStatusColumn(true));
                super.addHasAttachmentColumn();
                super.addHasSampleColumn();
                super.addHasSequenceColumn();
                columns.add(super.addCreatedColumn(true));
                return columns;
            }
        };

        panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.setStyleName("margin-top-20");
        initWidget(panel);
        initView();
    }

    public void setContent(Widget widget) {
        panel.clear();
        panel.add(widget);
    }

    public void initView() {
        panel.clear();
        panel.add(table);
        panel.add(table.getPager());
    }

    public CollectionDataTable getTable() {
        return table;
    }
}
