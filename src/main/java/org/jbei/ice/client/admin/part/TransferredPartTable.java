package org.jbei.ice.client.admin.part;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.common.table.column.EntryPartIdColumn;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Hector Plahar
 */
public class TransferredPartTable extends EntryDataTable<PartData> {

    private final EntryTablePager pager;

    public TransferredPartTable(ServiceDelegate<PartData> delegate) {
        super(delegate);
        this.pager = new EntryTablePager();
        pager.setDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<PartData, ?>> createColumns(ServiceDelegate<PartData> serviceDelegate) {
        ArrayList<DataTableColumn<PartData, ?>> columns = new ArrayList<DataTableColumn<PartData, ?>>();

        columns.add(super.addSelectionColumn());
        columns.add(super.addTypeColumn(true, 60, Unit.PX));
        DataTableColumn<PartData, PartData> partIdCol = addPartIdColumn(serviceDelegate, true, 120, Unit.PX);
        columns.add(partIdCol);
        columns.add(super.addNameColumn(120, Unit.PX));
        columns.add(super.addSummaryColumn());
        columns.add((addWebPartnerName()));
        columns.add(super.addStatusColumn());
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        columns.add(super.addCreatedColumn());

        return columns;
    }

    protected DataTableColumn<PartData, PartData> addPartIdColumn(ServiceDelegate<PartData> delegate,
            boolean sortable, double width, Unit unit) {
        PartIDCell<PartData> cell = new PartIDCell<PartData>(delegate);
        DataTableColumn<PartData, PartData> partIdColumn = new EntryPartIdColumn<PartData>(cell);
        this.setColumnWidth(partIdColumn, width, unit);
        partIdColumn.setSortable(sortable);
        this.addColumn(partIdColumn, "Part ID");
        return partIdColumn;
    }

    protected DataTableColumn<PartData, SafeHtml> addWebPartnerName() {
        SafeHtmlCell htmlCell = new SafeHtmlCell();
        DataTableColumn<PartData, SafeHtml> partner =
                new DataTableColumn<PartData, SafeHtml>(htmlCell, ColumnField.ICE_PROJECT) {

                    @Override
                    public SafeHtml getValue(PartData object) {
                        String projectName = null; //object.getWebPartnerName();
                        String projectURI = null; //object.getWebPartnerURL();
                        if (projectName == null && projectURI == null)
                            return SafeHtmlUtils.EMPTY_SAFE_HTML;

                        if (projectURI == null)
                            return SafeHtmlUtils.fromSafeConstant("<i>" + projectName + "</i>");

                        String name = (projectName == null || projectName.isEmpty()) ? projectURI : projectName;
                        return SafeHtmlUtils.fromSafeConstant(
                                "<a target=\"_blank\" href=\"" + projectURI + "\">" + name
                                        + "</a>&nbsp;<i class=\""
                                        + FAIconType.EXTERNAL_LINK.getStyleName() + " opacity_hover\"></i>");
                    }
                };

        this.addColumn(partner, "Registry");
        return partner;
    }

    public EntryTablePager getPager() {
        return this.pager;
    }
}
