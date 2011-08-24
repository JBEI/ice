package org.jbei.ice.client.view;

import java.util.ArrayList;

import org.jbei.ice.client.component.EntryTable;
import org.jbei.ice.client.component.SearchFilterPanel;
import org.jbei.ice.client.presenter.AdvancedSearchPresenter;
import org.jbei.ice.shared.FilterTrans;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AdvancedSearchView extends Composite implements AdvancedSearchPresenter.Display {

    private final SearchFilterPanel filterPanel;
    private final EntryTable table;

    public AdvancedSearchView() {

        // panels
        VerticalPanel vPanel = new VerticalPanel();
        initWidget(vPanel);

        filterPanel = new SearchFilterPanel();
        vPanel.add(filterPanel);
        vPanel.setStyleName("center");

        table = new EntryTable();
        //        vPanel.add(table);

        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        SimplePager pager = new SimplePager(TextLocation.LEFT, pagerResources, false, 0, true);
        pager.setDisplay(table);
        vPanel.add(pager);
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
    public EntryTable getResultsTable() {
        return this.table;
    }

    @Override
    public ArrayList<FilterTrans> getSearchFilters() {
        return filterPanel.getFilters();
    }
}
