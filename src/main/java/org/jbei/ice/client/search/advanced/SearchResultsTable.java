package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.client.common.table.cell.EntryOwnerCell;
import org.jbei.ice.client.common.table.cell.HasEntryPartIDCell;
import org.jbei.ice.client.common.table.cell.SearchRelevanceCell;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.common.table.column.HasEntryPartIdColumn;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.search.SearchResultInfo;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;

/**
 * Table for displaying search results. Default sort is by relevance
 *
 * @author Hector Plahar
 */
public class SearchResultsTable extends HasEntryDataTable<SearchResultInfo> {

    private final EntryTablePager pager;

    public SearchResultsTable(ServiceDelegate<SearchResultInfo> delegate) {
        super(delegate);
        this.pager = new EntryTablePager();
        pager.setDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<SearchResultInfo, ?>> createColumns(ServiceDelegate<SearchResultInfo>
            delegate) {

        ArrayList<DataTableColumn<SearchResultInfo, ?>> columns = new ArrayList<DataTableColumn<SearchResultInfo, ?>>();

        // selection column
        columns.add(super.addSelectionColumn());

        columns.add(addScoreColumn());

        // type column
        columns.add(super.addTypeColumn(true));

        // part id column. tooltip shows more info about entry
        columns.add(addPartIdColumn(delegate, false, 120, Unit.PX));

        // name column
        columns.add(super.addNameColumn(120, Unit.PX));

        columns.add(addSummaryColumn());
        columns.add(addOwnerColumn());
        super.addHasAttachmentColumn();
        super.addHasSampleColumn();
        super.addHasSequenceColumn();
        columns.add(super.addCreatedColumn(true));

        return columns;
    }

    protected DataTableColumn<SearchResultInfo, SearchResultInfo> addPartIdColumn(ServiceDelegate<SearchResultInfo>
            delegate,
            boolean sortable, double width, Unit unit) {
        HasEntryPartIDCell<SearchResultInfo> cell = new HasEntryPartIDCell<SearchResultInfo>(delegate) {
            @Override
            protected String getURI(SearchResultInfo value) {
                return value.getWebPartnerURL();
            }
        };
        DataTableColumn<SearchResultInfo, SearchResultInfo> partIdColumn =
                new HasEntryPartIdColumn<SearchResultInfo>(cell);
        this.setColumnWidth(partIdColumn, width, unit);
        partIdColumn.setSortable(sortable);
        this.addColumn(partIdColumn, "Part ID");
        return partIdColumn;
    }

    protected DataTableColumn<SearchResultInfo, SafeHtml> addSummaryColumn() {
        DataTableColumn<SearchResultInfo, SafeHtml> summaryColumn = new DataTableColumn<SearchResultInfo, SafeHtml>(
                new SafeHtmlCell(),
                ColumnField.SUMMARY) {

            @Override
            public SafeHtml getValue(SearchResultInfo object) {
                String description = object.getEntryInfo().getShortDescription();
                if (description == null)
                    return SafeHtmlUtils.EMPTY_SAFE_HTML;

                int size = (Window.getClientWidth() - 1200);
                if (size <= 0)
                    size = 250;

                return SafeHtmlUtils
                        .fromSafeConstant("<div style=\"width: "
                                                  + size
                                                  + "px; white-space: nowrap; overflow: hidden; text-overflow: "
                                                  + "ellipsis;\" title=\""
                                                  + description.replaceAll("\"", "'") + "\">"
                                                  + description + "</div>");
            }
        };

        this.addColumn(summaryColumn, "Summary");
        return summaryColumn;
    }

    protected DataTableColumn<SearchResultInfo, SearchResultInfo> addOwnerColumn() {

        EntryOwnerCell<SearchResultInfo> cell = new EntryOwnerCell<SearchResultInfo>() {

            @Override
            public String getOwnerName(SearchResultInfo value) {
                return value.getEntryInfo().getOwner();
            }

            @Override
            public String getOwnerId(SearchResultInfo value) {
                if (value.getEntryInfo().getOwnerId() == 0)
                    return "";

                return value.getEntryInfo().getOwnerId() + "";
            }
        };

        DataTableColumn<SearchResultInfo, SearchResultInfo> ownerColumn = new DataTableColumn<SearchResultInfo,
                SearchResultInfo>(
                cell, ColumnField.OWNER) {

            @Override
            public SearchResultInfo getValue(SearchResultInfo object) {
                return object;
            }
        };

        this.addColumn(ownerColumn, "Owner");
        ownerColumn.setSortable(false);
        this.setColumnWidth(ownerColumn, 180, Unit.PX);
        return ownerColumn;
    }

    protected DataTableColumn<SearchResultInfo, SearchResultInfo> addScoreColumn() {

        SearchRelevanceCell<SearchResultInfo> cell = new SearchRelevanceCell<SearchResultInfo>() {
            @Override
            public String getHTML(SearchResultInfo value) {
                float maxScore = value.getMaxScore();
                float score = value.getScore();

                int r = (int) (score / maxScore * 100);

                if (maxScore < 0.8) {
                    r = r * 90 / 100; // reduce by 10% to show that results are not that fantastic
                }

                if (r == 0)
                    r = 1;

                String color = "background-color: ";
                if (r <= 10)
                    color += "red";
                else if (r > 10 && r <= 25)
                    color += "lightCoral";
                else if (r > 25 && r <= 60)
                    color += "#ffee75";
                else if (r > 60 && r <= 80)
                    color += "yellowgreen";
                else
                    color += "green";

                return "<div style=\"opacity: 0.85; display: inline-block; width: " + r + "%;"
                        + color + "\">&nbsp;</div>";
            }
        };

        DataTableColumn<SearchResultInfo, SearchResultInfo> scoreColumn = new DataTableColumn<SearchResultInfo,
                SearchResultInfo>(
                cell,
                ColumnField.RELEVANCE) {
            @Override
            public SearchResultInfo getValue(SearchResultInfo object) {
                return object;
            }
        };

        scoreColumn.setSortable(true);
        this.addColumn(scoreColumn, "Relevance");
        this.setColumnWidth(scoreColumn, 90, Unit.PX);
        return scoreColumn;
    }

    public EntryTablePager getPager() {
        return this.pager;
    }
}
