package org.jbei.ice.client.view;

import java.util.ArrayList;

import org.jbei.ice.client.component.EntryTable;
import org.jbei.ice.client.component.EntryTablePager;
import org.jbei.ice.client.component.ExportAsPanel;
import org.jbei.ice.client.component.SearchFilterPanel;
import org.jbei.ice.client.panel.Footer;
import org.jbei.ice.client.panel.Header;
import org.jbei.ice.client.panel.HeaderMenu;
import org.jbei.ice.client.presenter.AdvancedSearchPresenter;
import org.jbei.ice.shared.FilterTrans;

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
    private EntryTable table;
    private EntryTablePager pager;
    private ExportAsPanel exportPanel;

    public AdvancedSearchView() {

        // page layout
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        layout.setHeight("98%");
        initWidget(layout);

        // headers
        layout.setWidget(0, 0, new Header());
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
        captionPanel.setStyleName("center");
        contents.setWidget(0, 0, captionPanel);

        // add results table
        VerticalPanel panel = new VerticalPanel();
        table = new EntryTable();
        table.setWidth("100%", true);
        panel.add(table);

        // table pager
        pager = new EntryTablePager();
        pager.setDisplay(table);
        panel.add(pager);
        exportPanel = new ExportAsPanel(table);
        panel.add(exportPanel);
        contents.setWidget(1, 0, panel);

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
    public EntryTable getResultsTable() {
        return this.table;
    }

    @Override
    public ArrayList<FilterTrans> getSearchFilters() {
        return filterPanel.getFilters();
    }

    @Override
    public EntryTablePager getPager() {
        return pager;
    }

    @Override
    public ExportAsPanel getExportOptions() {
        return exportPanel;
    }
}
