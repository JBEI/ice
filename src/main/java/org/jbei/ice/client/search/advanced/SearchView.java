package org.jbei.ice.client.search.advanced;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.search.blast.BlastResultsTable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class SearchView extends Composite implements ISearchView {

    private final FlexTable layout;
    private final HTMLPanel tableHeader;
    private final HTML local;
    private final HTML web;

    public SearchView() {
        layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        local = new HTML("Local Results");
        local.setStyleName("web_results_header_selected");
        web = new HTML("<i class=\"" + FAIconType.GLOBE.getStyleName()
                               + " opacity_hover\"></i> <a class=\"cell_mouseover\">Search Other Registries</a>");
        web.setStyleName("web_results_header");

        // table header
        String html = "<span id=\"local_results_link\"></span><span id=\"web_results_link\"></span>";
        tableHeader = new HTMLPanel(html);
        tableHeader.setStyleName("web_header");
        tableHeader.add(local, "local_results_link");
        tableHeader.add(web, "web_results_link");

        // add a break between filters and results
        layout.setWidget(0, 0, tableHeader);
        tableHeader.setVisible(false);

        String noQueryHTML = "<div style=\"line-height: 1px; opacity: 0.7\">"
                + "<i class=\"icon-exclamation-sign \" style=\"font-size: 9em; color: orange\"></i>"
                + "<br><h2>NO SEARCH QUERY DETECTED</h2>"
                + "<h5>PLEASE ENTER SEARCH TERMS AND/OR USE THE FILTERS IN THE DROP DOWN MENU</h5></div>";

        layout.setHTML(1, 0, noQueryHTML);
        layout.getFlexCellFormatter().setAlignment(1, 0, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);
    }

    @Override
    public void showWebOfRegistryOptions(boolean show) {
        tableHeader.setVisible(show);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setLocalSearchHandler(final ClickHandler handler) {
        local.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                local.setStyleName("web_results_header_selected");
                local.setHTML("Local Results");
                web.setStyleName("web_results_header");
                web.setHTML("<i class=\"" + FAIconType.GLOBE.getStyleName()
                                    + " opacity_hover\"></i> <a class=\"cell_mouseover\">Search Other Registries</a>");
                handler.onClick(event);
            }
        });
    }

    @Override
    public void setWebSearchHandler(final ClickHandler handler) {
        web.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                local.setStyleName("web_results_header");
                local.setHTML("<i class=\"" + FAIconType.SEARCH.getStyleName()
                                      + " opacity_hover\"></i> <a class=\"cell_mouseover\">Search Locally</a>");
                web.setStyleName("web_results_header_selected");
                web.setHTML("<i class=\""
                                    + FAIconType.GLOBE.getStyleName()
                                    + " opacity_hover\"></i> Results From Other Registries");
                handler.onClick(event);
            }
        });
    }

    @Override
    public void setSearchVisibility(SearchResultsTable table, boolean visible) {
        table.getPager().setVisible(visible);
        table.setVisible(visible);

        if (visible) {
            layout.setWidget(1, 0, table);
            layout.setWidget(2, 0, table.getPager());
            History.newItem(Page.QUERY.getLink(), false);
        }
    }

    @Override
    public void setBlastVisibility(BlastResultsTable blastTable, boolean visible) {
        blastTable.setVisible(visible);
        blastTable.getPager().setVisible(visible);

        if (visible) {
            layout.setWidget(1, 0, blastTable);
            layout.setWidget(2, 0, blastTable.getPager());
            History.newItem(Page.QUERY.getLink(), false);
        }
    }
}
