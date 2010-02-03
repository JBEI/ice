package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
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
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.dataProviders.EntriesDataProvider;
import org.jbei.ice.web.pages.EntryAllExcelExportPage;
import org.jbei.ice.web.pages.EntryExcelExportPage;
import org.jbei.ice.web.pages.EntryNewPage;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.EntryXMLExportPage;
import org.jbei.ice.web.pages.PrintableEntriesFullContentPage;
import org.jbei.ice.web.pages.PrintableEntriesTablePage;
import org.jbei.ice.web.pages.ProfilePage;

public class MostRecentEntriesPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private EntriesDataProvider sortableDataProvider;
    private DataView<Entry> dataView;

    public MostRecentEntriesPanel(String id) {
        super(id);

        sortableDataProvider = new EntriesDataProvider();
        sortableDataProvider.setSort("created", false);

        dataView = new DataView<Entry>("entriesDataView", sortableDataProvider, 15) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(Item<Entry> item) {
                Entry entry = (Entry) item.getModelObject();

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
                item.add(new Label("description", entry.getShortDescription()));
                item.add(new Label("status", JbeiConstants.getStatus(entry.getStatus())));

                add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery-ui-1.7.2.custom.min.js"));
                add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery.cluetip.js"));
                add(CSSPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery.cluetip.css"));

                ResourceReference blankImage = new ResourceReference(MostRecentEntriesPanel.class,
                        "blank.png");
                ResourceReference hasAttachmentImage = new ResourceReference(
                        MostRecentEntriesPanel.class, "attachment.gif");
                ResourceReference hasSequenceImage = new ResourceReference(
                        MostRecentEntriesPanel.class, "sequence.gif");
                ResourceReference hasSampleImage = new ResourceReference(
                        MostRecentEntriesPanel.class, "sample.png");

                ResourceReference hasAttachment = (AttachmentManager.hasAttachment(entry)) ? hasAttachmentImage
                        : blankImage;
                item.add(new Image("hasAttachment", hasAttachment));

                ResourceReference hasSequence = (SequenceManager.hasSequence(entry)) ? hasSequenceImage
                        : blankImage;
                item.add(new Image("hasSequence", hasSequence));

                ResourceReference hasSample = (SampleManager.hasSample(entry)) ? hasSampleImage
                        : blankImage;
                item.add(new Image("hasSample", hasSample));

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(entry.getCreationTime());
                item.add(new Label("date", dateString));
            }
        };

        add(dataView);

        add(new JbeiPagingNavigator("navigator", dataView));

        add(new OrderByBorder("orderByType", "type", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                dataView.setCurrentPage(0);
            }
        });

        add(new OrderByBorder("orderBySummary", "summary", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                dataView.setCurrentPage(0);
            }
        });

        add(new OrderByBorder("orderByOwner", "owner", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                dataView.setCurrentPage(0);
            }
        });

        add(new OrderByBorder("orderByStatus", "status", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                dataView.setCurrentPage(0);
            }
        });

        add(new OrderByBorder("orderByCreated", "created", sortableDataProvider) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                dataView.setCurrentPage(0);
            }
        });

        add(new Link<Page>("printableCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesTablePage(sortableDataProvider.getEntries(),
                        true));
            }
        });

        add(new Link<Page>("printableAllLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesFullContentPage(sortableDataProvider
                        .getEntries()));
            }
        });

        add(new Link<Page>("excelCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntryExcelExportPage(sortableDataProvider.getEntries()));
            }
        });

        add(new Link<Page>("excelAllLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntryAllExcelExportPage(sortableDataProvider.getEntries()));
            }
        });

        add(new Link<Page>("xmlLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntryXMLExportPage(sortableDataProvider.getEntries()));
            }
        });
    }
}
