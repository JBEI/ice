package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.search.SearchFilterPanel;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.event.SearchSelectionHandler;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdvancedSearchView extends AbstractLayout implements IAdvancedSearchView {

    private SearchFilterPanel filterPanel;
    private AdvancedSearchResultsTable table;
    private EntryTablePager pager;
    private HorizontalPanel header; // TODO : another table
    private VerticalPanel resultsPanel;

    public AdvancedSearchView() {
    }

    @Override
    public void initComponents() {
        super.initComponents();
        header = new HorizontalPanel();
        resultsPanel = new VerticalPanel();
    }

    @Override
    protected Widget createContents() {

        FlexTable contents = new FlexTable();
        contents.setCellPadding(0);
        contents.setCellSpacing(0);
        contents.setWidth("100%");

        // add filters
        CaptionPanel captionPanel = new CaptionPanel("Filters");
        captionPanel.setWidth("100%");

        filterPanel = new SearchFilterPanel();
        captionPanel.setWidth("500px");
        captionPanel.add(filterPanel);
        //        captionPanel.setStyleName("center");
        contents.setWidget(0, 0, captionPanel);
        contents.getFlexCellFormatter().setHorizontalAlignment(0, 0,
            HasHorizontalAlignment.ALIGN_CENTER);

        // add results table
        resultsPanel.setWidth("100%");
        resultsPanel.add(header);
        table = new AdvancedSearchResultsTable();
        table.setWidth("100%", true);
        resultsPanel.add(table);

        // add a break between filters and results
        contents.setHTML(1, 0, "<br />");

        // table pager
        pager = new EntryTablePager();
        pager.setDisplay(table);
        resultsPanel.add(pager);

        contents.setWidget(2, 0, resultsPanel);

        return contents;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public Button getEvaluateButton() {
        return this.filterPanel.getEvaluateButton();
    }

    @Override
    public AdvancedSearchResultsTable getResultsTable() {
        return this.table;
    }

    @Override
    public ArrayList<SearchFilterInfo> getSearchFilters() {
        return filterPanel.getFilters();
    }

    @Override
    public void setResultsVisibility(boolean visible) {
        //        this.table.setVisible(visible);
        //        this.pager.setVisible(visible);
        resultsPanel.setVisible(visible);
    }

    @Override
    public void setSelectionMenu(Widget menu) {
        header.add(menu);
    }

    @Override
    public void setFilterPanelChangeHandler(SearchSelectionHandler handler) {

    }

}
