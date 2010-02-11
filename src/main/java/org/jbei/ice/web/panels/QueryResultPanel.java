package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.dataProviders.EntriesQueryDataProvider;
import org.jbei.ice.web.pages.EntriesAllFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesCurrentFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesXMLExportPage;
import org.jbei.ice.web.pages.PrintableEntriesFullContentPage;
import org.jbei.ice.web.pages.PrintableEntriesTablePage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class QueryResultPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private AbstractEntriesDataView<Entry> entriesDataView;
    private EntriesQueryDataProvider sortableDataProvider;

    ResourceReference blankImage;
    ResourceReference hasAttachmentImage;
    ResourceReference hasSequenceImage;
    ResourceReference hasSampleImage;

    public QueryResultPanel(String id, ArrayList<String[]> queries) {
        super(id);

        updateView(queries);

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));

        blankImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "blank.png");
        hasAttachmentImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "attachment.gif");
        hasSequenceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sequence.gif");
        hasSampleImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sample.png");

        add(new Image("attachmentHeaderImage", hasAttachmentImage));
        add(new Image("sequenceHeaderImage", hasSequenceImage));
        add(new Image("sampleHeaderImage", hasSampleImage));
    }

    public void updateView(ArrayList<String[]> queries) {
        sortableDataProvider = new EntriesQueryDataProvider(queries);

        entriesDataView = new AbstractEntriesDataView<Entry>("entriesDataView",
                sortableDataProvider, 15) {
            private static final long serialVersionUID = 1L;

            protected Entry getEntry(Item<Entry> item) {
                return item.getModelObject();
            }

            @Override
            protected void populateItem(Item<Entry> item) {
                super.populateItem(item);

                renderOwnerLink(item);
            }
        };

        entriesDataView.setOutputMarkupId(true);

        addOrReplace(entriesDataView);
        addOrReplace(getNavigation(entriesDataView));

        renderSortableColumns();

        renderExportLinks();
    }

    private void renderSortableColumns() {
        addOrReplace(new OrderByBorder("orderByType", "type", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                entriesDataView.setCurrentPage(0);
            }
        });

        addOrReplace(new OrderByBorder("orderBySummary", "summary", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                entriesDataView.setCurrentPage(0);
            }
        });

        addOrReplace(new OrderByBorder("orderByOwner", "owner", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                entriesDataView.setCurrentPage(0);
            }
        });

        addOrReplace(new OrderByBorder("orderByStatus", "status", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                entriesDataView.setCurrentPage(0);
            }
        });

        addOrReplace(new OrderByBorder("orderByCreated", "created", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                entriesDataView.setCurrentPage(0);
            }
        });
    }

    private void renderExportLinks() {
        addOrReplace(new Link<Page>("printableCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesTablePage(sortableDataProvider.getEntries(),
                        true));
            }
        });

        addOrReplace(new Link<Page>("printableAllLink") {
            private static final long serialVersionUID = 2L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesFullContentPage(sortableDataProvider
                        .getEntries()));
            }
        });

        addOrReplace(new Link<Page>("excelCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesCurrentFieldsExcelExportPage(sortableDataProvider
                        .getEntries()));
            }
        });

        addOrReplace(new Link<Page>("excelAllLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesAllFieldsExcelExportPage(sortableDataProvider
                        .getEntries()));
            }
        });

        addOrReplace(new Link<Page>("xmlLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesXMLExportPage(sortableDataProvider.getEntries()));
            }
        });
    }

    private JbeiPagingNavigator getNavigation(DataView<Entry> dataView) {
        return new JbeiPagingNavigator("navigator", dataView);
    }
}
