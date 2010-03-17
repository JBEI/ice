package org.jbei.ice.web.panels;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.lucene.SearchResult;
import org.jbei.ice.web.dataProviders.SearchDataProvider;
import org.jbei.ice.web.pages.EntriesAllFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesCurrentFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesXMLExportPage;
import org.jbei.ice.web.pages.PrintableEntriesFullContentPage;
import org.jbei.ice.web.pages.PrintableEntriesTablePage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class SearchResultPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private SearchDataProvider searchDataProvider;
    private AbstractEntriesDataView<SearchResult> searchEntriesDataView;

    ResourceReference blankImage;
    ResourceReference hasAttachmentImage;
    ResourceReference hasSequenceImage;
    ResourceReference hasSampleImage;

    public SearchResultPanel(String id, ArrayList<SearchResult> searchResults, int limit) {
        super(id);

        blankImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "blank.png");
        hasAttachmentImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "attachment.gif");
        hasSequenceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sequence.gif");
        hasSampleImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sample.png");

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));

        add(new Image("attachmentHeaderImage", hasAttachmentImage));
        add(new Image("sequenceHeaderImage", hasSequenceImage));
        add(new Image("sampleHeaderImage", hasSampleImage));

        searchDataProvider = new SearchDataProvider(searchResults);

        searchEntriesDataView = new AbstractEntriesDataView<SearchResult>("entriesDataView",
                searchDataProvider, limit) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Entry getEntry(Item<SearchResult> item) {
                SearchResult searchResult = item.getModelObject();

                return searchResult.getEntry();
            }

            @Override
            protected void populateItem(Item<SearchResult> item) {
                super.populateItem(item);

                renderSearchScore(item);
                renderOwnerLink(item);
            }

            private void renderSearchScore(Item<SearchResult> item) {
                NumberFormat formatter = new DecimalFormat("##");

                String scoreString = formatter.format(item.getModelObject().getScore() * 100);

                item.add(new Label("score", scoreString));
            }
        };

        add(searchEntriesDataView);

        add(new JbeiPagingNavigator("navigator", searchEntriesDataView));

        renderExportLinks();
    }

    private void renderExportLinks() {
        add(new Link<Page>("printableCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesTablePage(searchDataProvider.getEntries(), true));
            }
        });

        add(new Link<Page>("printableAllLink") {
            private static final long serialVersionUID = 2L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesFullContentPage(searchDataProvider.getEntries()));
            }
        });

        add(new Link<Page>("excelCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesCurrentFieldsExcelExportPage(searchDataProvider
                        .getEntries()));
            }
        });

        add(new Link<Page>("excelAllLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesAllFieldsExcelExportPage(searchDataProvider.getEntries()));
            }
        });

        add(new Link<Page>("xmlLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesXMLExportPage(searchDataProvider.getEntries()));
            }
        });
    }
}
