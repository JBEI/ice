package org.jbei.ice.web.panels;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.SearchResult;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.dataProviders.SearchDataProvider;
import org.jbei.ice.web.pages.EntriesAllFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesCurrentFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesXMLExportPage;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.PrintableEntriesFullContentPage;
import org.jbei.ice.web.pages.PrintableEntriesTablePage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class SearchResultPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private SearchDataProvider searchDataProvider;

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
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.css"));

        add(new Image("attachmentHeaderImage", hasAttachmentImage));
        add(new Image("sequenceHeaderImage", hasSequenceImage));
        add(new Image("sampleHeaderImage", hasSampleImage));

        searchDataProvider = new SearchDataProvider(searchResults);

        DataView<SearchResult> dataView = new DataView<SearchResult>("entriesDataView",
                searchDataProvider, limit) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(Item<SearchResult> item) {
                SearchResult searchResult = (SearchResult) item.getModelObject();
                Entry entry = null;
                try {
                    entry = EntryManager.getByRecordId(searchResult.getRecordId());
                } catch (ManagerException e1) {
                    e1.printStackTrace();

                    return;
                }

                item.add(new Label("index", ""
                        + (getItemsPerPage() * getCurrentPage() + item.getIndex() + 1)));
                item.add(new Label("recordType", entry.getRecordType()));

                BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>(
                        "partIdLink", EntryViewPage.class, new PageParameters("0=" + entry.getId()));
                entryLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
                item.add(entryLink);

                item.add(new Label("name", entry.getOneName().getName()));

                NumberFormat formatter = new DecimalFormat("##");
                String scoreString = formatter.format(searchResult.getScore() * 100);
                item.add(new Label("score", scoreString));

                item.add(new Label("description", entry.getShortDescription()));
                Account ownerAccount = null;

                try {
                    ownerAccount = AccountManager.getByEmail(entry.getOwnerEmail());
                } catch (ManagerException e) {
                    e.printStackTrace();
                }

                BookmarkablePageLink<ProfilePage> ownerProfileLink = new BookmarkablePageLink<ProfilePage>(
                        "ownerProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                                + entry.getOwnerEmail()));
                ownerProfileLink.add(new Label("owner", (ownerAccount != null) ? ownerAccount
                        .getFullName() : entry.getOwner()));
                String ownerAltText = "Profile "
                        + ((ownerAccount == null) ? entry.getOwner() : ownerAccount.getFullName());
                ownerProfileLink.add(new SimpleAttributeModifier("title", ownerAltText));
                ownerProfileLink.add(new SimpleAttributeModifier("alt", ownerAltText));
                item.add(ownerProfileLink);

                item.add(new Label("status", JbeiConstants.getStatus(entry.getStatus())));

                item
                        .add(new Image("hasAttachment",
                                (AttachmentManager.hasAttachment(entry)) ? hasAttachmentImage
                                        : blankImage));
                item.add(new Image("hasSequence",
                        (SequenceManager.hasSequence(entry)) ? hasSequenceImage : blankImage));
                item.add(new Image("hasSample", (SampleManager.hasSample(entry)) ? hasSampleImage
                        : blankImage));

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(entry.getCreationTime());
                item.add(new Label("date", dateString));
            }
        };

        add(dataView);

        add(new JbeiPagingNavigator("navigator", dataView));

        add(new Link<Page>("printableCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesTablePage(searchDataProvider.getEntries(), true));
            }
        });

        add(new Link<Page>("printableAllLink") {
            private static final long serialVersionUID = 1L;

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
