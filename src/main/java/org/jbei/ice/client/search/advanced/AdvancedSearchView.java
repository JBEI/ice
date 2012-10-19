package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.common.header.BlastSearchFilter;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.search.blast.BlastResultsTable;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class AdvancedSearchView extends Composite implements IAdvancedSearchView {

    private FlowPanel filterPanel;
    private EntryTablePager pager;
    private final FlexTable layout;

    public AdvancedSearchView() {
        layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        initComponents();

        // add filters
        CaptionPanel captionPanel = new CaptionPanel(
                "<span class=\"search_caption_caption\">Search Filters</span>", true);
        captionPanel.setWidth("98%");
        captionPanel.setStyleName("search_caption_display");
        captionPanel.add(filterPanel);
        layout.setWidget(0, 0, captionPanel);

        // add a break between filters and results
        layout.setHTML(1, 0, "&nbsp;");
        // TODO : loading indicator?
    }

    protected void initComponents() {
        filterPanel = new FlowPanel();
        filterPanel.setWidth("100%");
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setSearchFilters(ArrayList<SearchFilterInfo> filters) {
        filterPanel.clear();
        int size = filters.size();
        int i = 0;

        for (SearchFilterInfo filter : filters) {

            if (isBlast(filter)) {
                filterPanel.add(new BlastSearchFilter(filter.getOperand(), filter.getOperator()));
            } else {
                String filterString = "";
                if (filter.getType() != null)
                    filterString += filter.getType();

                if (filter.getOperator() != null)
                    filterString += filter.getOperator();

                if (filter.getOperand() != null)
                    filterString += filter.getOperand();

                Label label;
                if (i == size - 1)
                    label = new Label(filterString);
                else
                    label = new Label(filterString + ", ");

                label.setStyleName("search_caption_display_content");
                filterPanel.add(label);
            }
            i += 1;
        }
    }

    private boolean isBlast(SearchFilterInfo filter) {
        return QueryOperator.BLAST_N.symbol().equals(filter.getType())
                || QueryOperator.TBLAST_X.symbol().equals(filter.getType());
    }

    @Override
    public void setSearchVisibility(AdvancedSearchResultsTable table, boolean visible) {
        if (pager == null) {
            pager = new EntryTablePager();
            pager.setDisplay(table);
        }

        pager.setVisible(visible);
        table.setVisible(visible);

        if (visible) {
//            table.clearSelection();
            layout.setWidget(2, 0, table);
            layout.setWidget(3, 0, pager);
        }
    }

    @Override
    public void setBlastVisibility(BlastResultsTable blastTable, boolean visible) {
        blastTable.setVisible(visible);
        blastTable.getPager().setVisible(visible);

        if (visible) {
            layout.setWidget(2, 0, blastTable);
            layout.setWidget(3, 0, blastTable.getPager());
        }
    }
}
