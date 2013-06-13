package org.jbei.ice.client.search.advanced;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.header.HeaderView;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.search.blast.BlastResultsTable;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.search.BlastProgram;
import org.jbei.ice.shared.dto.search.BlastQuery;
import org.jbei.ice.shared.dto.search.SearchQuery;

import java.util.ArrayList;

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
        }
    }

    @Override
    public void setBlastVisibility(BlastResultsTable blastTable, boolean visible) {
        blastTable.setVisible(visible);
        blastTable.getPager().setVisible(visible);

        if (visible) {
            layout.setWidget(1, 0, blastTable);
            layout.setWidget(2, 0, blastTable.getPager());
        }
    }

    @Override
    public SearchQuery parseUrlForQuery() {
        String token = History.getToken();
        String[] split = token.split(ClientController.URL_SEPARATOR);
        if (split.length < 2)
            return new SearchQuery();

        SearchQuery query = new SearchQuery();
        String[] filters = split[1].split("&");
        String textQuery = URL.decodeQueryString(filters[0]);
        HeaderView.getInstance().setSearchBox(textQuery);
        query.setQueryString(textQuery);

        for (int i = 1; i < filters.length; i += 1) {
            String decoded = URL.decode(filters[i]);

            // check blast
            BlastProgram blastProgram = BlastProgram.filterValueOf(decoded);
            if (blastProgram != null) {
                query.setBlastQuery(new BlastQuery(blastProgram, ClientController.blast));
                continue;
            }

            // types (e.g. Strain / Plasmid only)
            if (decoded.startsWith("type")) {
                String[] typeSplit = decoded.split("=");
                if (typeSplit.length < 2)
                    continue;

                String types = typeSplit[1];
                if ("all".equals(types))
                    continue;

                ArrayList<EntryType> typesList = new ArrayList<EntryType>();
                for (String type : types.split(",")) {
                    EntryType entryType = EntryType.nameToType(type);
                    if (entryType == null)
                        continue;
                    typesList.add(entryType);
                }
                if (!typesList.isEmpty())
                    query.setEntryTypes(typesList);

                continue;
            }

            // sequence. attachment. sample
            if ("attachment".equalsIgnoreCase(decoded))
                query.getParameters().setHasAttachment(true);
            if ("sample".equalsIgnoreCase(decoded))
                query.getParameters().setHasSample(true);
            if ("sequence".equalsIgnoreCase(decoded))
                query.getParameters().setHasSequence(true);

            // biosafety
            if (decoded.startsWith("biosafety")) {
                String[] biosafetyString = decoded.split("=");
                if (biosafetyString.length < 2)
                    continue;

                Integer biosafety = (Integer.parseInt(biosafetyString[1]));
                query.setBioSafetyOption(BioSafetyOption.enumValue(biosafety));
            }
        }

        return query;
    }
}
