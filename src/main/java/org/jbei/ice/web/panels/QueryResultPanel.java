package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
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

    private ResourceReference hasAttachmentImage;
    private ResourceReference hasSequenceImage;
    private ResourceReference hasSampleImage;

    private Fragment resultsTableFragment;
    private Fragment noResultsFragment;

    public QueryResultPanel(String id, ArrayList<String[]> queries) {
        super(id);

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

        resultsTableFragment = new Fragment("resultsPanel", "resultsTableFragment", this);
        noResultsFragment = new Fragment("resultsPanel", "noResultsFragment", this);

        updateView(queries);

        resultsTableFragment.add(new Image("attachmentHeaderImage", hasAttachmentImage));
        resultsTableFragment.add(new Image("sequenceHeaderImage", hasSequenceImage));
        resultsTableFragment.add(new Image("sampleHeaderImage", hasSampleImage));
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

        resultsTableFragment.addOrReplace(entriesDataView);
        resultsTableFragment.addOrReplace(getNavigation(entriesDataView));

        renderExportLinks();

        if (sortableDataProvider.size() == 0) {
            addOrReplace(noResultsFragment);
        } else {
            addOrReplace(resultsTableFragment);
        }
    }

    private void renderExportLinks() {
        resultsTableFragment.addOrReplace(new Link<Page>("printableCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesTablePage(sortableDataProvider.getEntries(),
                        true));
            }
        });

        resultsTableFragment.addOrReplace(new Link<Page>("printableAllLink") {
            private static final long serialVersionUID = 2L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesFullContentPage(sortableDataProvider
                        .getEntries()));
            }
        });

        resultsTableFragment.addOrReplace(new Link<Page>("excelCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesCurrentFieldsExcelExportPage(sortableDataProvider
                        .getEntries()));
            }
        });

        resultsTableFragment.addOrReplace(new Link<Page>("excelAllLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesAllFieldsExcelExportPage(sortableDataProvider
                        .getEntries()));
            }
        });

        resultsTableFragment.addOrReplace(new Link<Page>("xmlLink") {
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
