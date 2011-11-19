package org.jbei.ice.client.search.advanced;

import java.util.ArrayList;

import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.common.HeaderView;
import org.jbei.ice.client.common.search.SearchFilterPanel;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.event.SearchSelectionHandler;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdvancedSearchView extends Composite implements AdvancedSearchPresenter.Display {

    private SearchFilterPanel filterPanel;
    private AdvancedSearchResultsTable table;
    private EntryTablePager pager;

    public AdvancedSearchView() {

        // page layout
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        layout.setHeight("98%");
        initWidget(layout);

        // headers
        layout.setWidget(0, 0, new HeaderView());
        layout.setWidget(1, 0, new HeaderMenu());

        // contents
        Widget contents = createSearchComponents();
        layout.setWidget(2, 0, contents);
        layout.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setHeight(2, 0, "100%");
        layout.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);

        // footer
        layout.setWidget(3, 0, Footer.getInstance());
    }

    protected Widget createSearchComponents() {

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
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        table = new AdvancedSearchResultsTable();
        table.setWidth("100%", true);
        panel.add(table);

        // add a break between filters and results
        contents.setHTML(1, 0, "<br />");

        // table pager
        pager = new EntryTablePager();
        pager.setDisplay(table);
        panel.add(pager);

        contents.setWidget(2, 0, panel);

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
        this.table.setVisible(visible);
        this.pager.setVisible(visible);
    }

    @Override
    public void setSelectionMenu(Widget menu) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFilterPanelChangeHandler(SearchSelectionHandler handler) {

    }
}
