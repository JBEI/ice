package org.jbei.ice.web.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.panels.UserEntriesViewPanel;

public class PrintableEntriesTablePage extends ProtectedPage {
    private ArrayList<Entry> entries;

    public PrintableEntriesTablePage(ArrayList<Entry> entries, boolean displayOwner) {
        super();

        this.entries = entries;

        if (displayOwner) {
            add(createWithOwnerTableFragment(displayOwner));
        } else {
            add(createWithoutOwnerTableFragment(displayOwner));
        }
    }

    @Override
    protected void initializeComponents() {
        add(new Label("title", "Printable"));
    }

    private Fragment createWithOwnerTableFragment(boolean displayOwner) {
        Fragment fragment = new Fragment("entriesFragmentPanel", "withOwnerFragment", this);

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);
        fragment.add(initializeDataView(displayOwner));

        return fragment;
    }

    private Fragment createWithoutOwnerTableFragment(boolean displayOwner) {
        Fragment fragment = new Fragment("entriesFragmentPanel", "withoutOwnerFragment", this);

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);
        fragment.add(initializeDataView(displayOwner));

        return fragment;
    }

    private ListView<Entry> initializeDataView(final boolean displayOwner) {
        ListView<Entry> entriesDataView = new ListView<Entry>("entriesDataView", entries) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Entry> item) {
                Entry entry = (Entry) item.getModelObject();

                item.add(new Label("index", String.valueOf(item.getIndex() + 1)));
                item.add(new Label("recordType", entry.getRecordType()));

                BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>(
                        "partIdLink", EntryViewPage.class, new PageParameters("0=" + entry.getId()));
                entryLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
                item.add(entryLink);

                item.add(new Label("name", entry.getOneName().getName()));

                item.add(new Label("description", entry.getShortDescription()));

                if (displayOwner) {
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
                            + ((ownerAccount == null) ? entry.getOwner() : ownerAccount
                                    .getFullName());
                    ownerProfileLink.add(new SimpleAttributeModifier("title", ownerAltText));
                    ownerProfileLink.add(new SimpleAttributeModifier("alt", ownerAltText));
                    item.add(ownerProfileLink);
                }

                item.add(new Label("status", JbeiConstants.getStatus(entry.getStatus())));

                add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery-ui-1.7.2.custom.min.js"));
                add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery.cluetip.js"));
                add(CSSPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery.cluetip.css"));

                ResourceReference blankImage = new ResourceReference(
                        PrintableEntriesTablePage.class, "blank.png");
                ResourceReference hasAttachmentImage = new ResourceReference(
                        UserEntriesViewPanel.class, "attachment.gif");
                ResourceReference hasSequenceImage = new ResourceReference(
                        UserEntriesViewPanel.class, "sequence.gif");
                ResourceReference hasSampleImage = new ResourceReference(
                        UserEntriesViewPanel.class, "sample.png");

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

        return entriesDataView;
    }
}
