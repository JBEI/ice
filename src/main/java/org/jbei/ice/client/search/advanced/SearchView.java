package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.common.header.BlastSearchFilter;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.search.blast.BlastResultsTable;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class SearchView extends Composite implements ISearchView {

    private FlowPanel filterPanel;
    private final FlexTable layout;
    private HorizontalPanel tableHeader;

    public SearchView() {
        layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        initComponents();

        // add filters
        CaptionPanel captionPanel = new CaptionPanel("<span class=\"search_caption\">Search Filters</span>", true);
        captionPanel.setWidth("98%");
        captionPanel.setStyleName("search_caption_display");
        captionPanel.add(filterPanel);

        // table header
        tableHeader = new HorizontalPanel();
        tableHeader.add(new HTML(
                "<span style=\"font-size: 12px; border-top: 1px solid #ccc; border-left: 1px solid #ccc;"
                        + " border-right: 1px solid #ccc\">&nbsp;Local Results&nbsp;</span>"));
        tableHeader.add(new HTML("<span><i class=\"" + FAIconType.GLOBE.getStyleName() + "\"></i> Web Results</span>"));

        // add a break between filters and results
        layout.setWidget(0, 0, captionPanel);
        layout.setWidget(1, 0, tableHeader);
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

        table.getPager().setVisible(visible);
        table.setVisible(visible);

        if (visible) {
            layout.setWidget(2, 0, table);
            layout.setWidget(3, 0, table.getPager());
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

    @Override
    public ArrayList<SearchFilterInfo> parseUrlForFilters() {
        String token = History.getToken();
        ArrayList<SearchFilterInfo> filterList = new ArrayList<SearchFilterInfo>();
        String[] split = token.split(";");
        if (split.length < 2)
            return filterList;

        String[] filters = split[1].split("&");
        for (int i = 0; i < filters.length; i += 1) {  // restrict to 2000 xter limit due to GET constraints
            // each filter is of the form "type operator operand"
            String decoded = URL.decode(filters[i]);
            if (i == 0) {
                // free form text;
                filterList.add(new SearchFilterInfo(null, null, decoded));
            } else {
//                String[] values = filter.split("=");
//                if (values != null && values.length >= 2)  {
//                    SearchFilterType type = SearchFilterType.stringToSearchType(values[0]);
//                    if( type != null ) {
//                        filterList.add(new SearchFilterInfo());
//                    }
//                }
            }
        }

        return filterList;
    }

    @Override
    public EntryType[] getSearchTypes() {
        String token = History.getToken();
        String[] split = token.split(AppController.URL_SEPARATOR);
        if (split.length < 2)
            return EntryType.values();

        String[] filters = split[1].split("&");
        String typesFilter = null;
        for (String filter : filters) {
            if (filter.startsWith("types")) {
                typesFilter = filter;
                break;
            }
        }
        if (typesFilter == null)
            return EntryType.values();

        String[] typeSplit = typesFilter.split("=");
        if (typeSplit.length < 2)
            return EntryType.values();

        String types = typeSplit[1];
        if ("all".equals(types))
            return EntryType.values();

        EntryType[] entryTypes = new EntryType[]{};
        int i = 0;
        for (String type : types.split(",")) {
            EntryType entryType = EntryType.nameToType(type);
            if (entryType == null)
                continue;
            entryTypes[i] = entryType;
            i += 1;
        }

        if (entryTypes.length == 0)
            return EntryType.values();
        return entryTypes;
    }
}
