package org.jbei.ice.client.common.table;

import java.util.Date;

import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.dto.HasEntryData;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Table whose elements consists of a type that
 * has a @see EntryDataView
 * 
 * @see HasEntryData
 * 
 * @author Hector Plahar
 * 
 */
public abstract class HasEntryDataTable<T extends HasEntryData> extends DataTable<T> {

    public HasEntryDataTable() {
    }

    protected DataTableColumn<String> addTypeColumn(boolean sortable) {
        DataTableColumn<String> typeCol = new DataTableColumn<String>(new TextCell(),
                ColumnField.TYPE) {

            @Override
            public String getValue(T entry) {
                return toUppercaseFully(entry.getDataView().getType());
            }
        };
        typeCol.setSortable(sortable);
        this.addColumn(typeCol, "Type");
        this.setColumnWidth(typeCol, 100, Unit.PX);
        return typeCol;
    }

    protected DataTableColumn<EntryData> addPartIdColumn(boolean sortable) {

        DataTableColumn<EntryData> partIdColumn = new DataTableColumn<EntryData>(
                new PartIDCell<EntryData>(), ColumnField.PART_ID) {

            @Override
            public EntryData getValue(T object) {
                return object.getDataView();
            }
        };

        this.setColumnWidth(partIdColumn, 100, Unit.PX);
        this.addColumn(partIdColumn, "Part ID");
        partIdColumn.setSortable(sortable);
        return partIdColumn;
    }

    protected DataTableColumn<String> addNameColumn() {
        DataTableColumn<String> nameColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.NAME) {

            @Override
            public String getValue(T object) {
                return object.getDataView().getName();
            }
        };

        nameColumn.setSortable(true);
        this.addColumn(nameColumn, "Name");
        this.setColumnWidth(nameColumn, 150, Unit.PX);
        return nameColumn;
    }

    protected DataTableColumn<String> addCreatedColumn() {
        DataTableColumn<String> createdColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.CREATED) {

            @Override
            public String getValue(HasEntryData object) {

                DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
                Date date = new Date(object.getDataView().getCreated());
                String value = format.format(date);
                if (value.length() >= 13)
                    value = (value.substring(0, 9) + "...");
                return value;
            }
        };

        createdColumn.setSortable(true);
        this.addColumn(createdColumn, "Created");
        this.setColumnWidth(createdColumn, 120, Unit.PX);
        return createdColumn;
    }

    private String toUppercaseFully(String value) {
        if (value == null || value.isEmpty())
            return "";
        return (value.substring(0, 1).toUpperCase() + value.substring(1));
    }

}
