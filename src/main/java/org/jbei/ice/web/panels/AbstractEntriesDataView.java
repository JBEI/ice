package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.AuthenticatedSampleManager;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.pages.UnprotectedPage;

public abstract class AbstractEntriesDataView<T> extends DataView<T> {
    private static final long serialVersionUID = 1L;

    public AbstractEntriesDataView(String id, IDataProvider<T> dataProvider, int perPage) {
        super(id, dataProvider, perPage);
    }

    protected abstract Entry getEntry(Item<T> item);

    @Override
    protected void populateItem(Item<T> item) {
        item.add(new SimpleAttributeModifier("class", item.getIndex() % 2 == 0 ? "odd_row"
                : "even_row"));

        renderIndex(item);
        renderEntryType(item);
        renderEntryName(item);
        renderEntryLink(item);
        renderImages(item);
        renderCreationTime(item);
        renderDescription(item);
        renderStatus(item);
    }

    protected void renderDescription(Item<T> item) {
        item.add(new Label("description", getEntry(item).getShortDescription()));
    }

    protected void renderStatus(Item<T> item) {
        item.add(new Label("status", JbeiConstants.getStatus(getEntry(item).getStatus())));
    }

    protected void renderEntryName(Item<T> item) {
        item.add(new Label("name", getEntry(item).getOneName().getName()));
    }

    protected void renderEntryType(Item<T> item) {
        item.add(new Label("recordType", getEntry(item).getRecordType()));
    }

    protected void renderOwnerLink(Item<T> item) {
        Entry entry = getEntry(item);

        Account ownerAccount = null;

        try {
            ownerAccount = AccountManager.getByEmail(entry.getOwnerEmail());
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        BookmarkablePageLink<ProfilePage> ownerProfileLink = new BookmarkablePageLink<ProfilePage>(
                "ownerProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                        + entry.getOwnerEmail()));
        ownerProfileLink.add(new Label("owner", (ownerAccount != null) ? ownerAccount.getFullName()
                : entry.getOwner()));

        if (ownerAccount != null) {
            String ownerAltText = "Profile " + ownerAccount.getFullName();
            ownerProfileLink.add(new SimpleAttributeModifier("title", ownerAltText));
            ownerProfileLink.add(new SimpleAttributeModifier("alt", ownerAltText));
        } else {
            ownerProfileLink.setEnabled(false);
        }

        item.add(ownerProfileLink);
    }

    protected void renderImages(Item<T> item) {
        Entry entry = getEntry(item);

        ResourceReference blankImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "blank.png");
        ResourceReference hasAttachmentImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "attachment.gif");
        ResourceReference hasSequenceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sequence.gif");
        ResourceReference hasSampleImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sample.png");

        item.add(new Image("hasAttachment",
                (AttachmentManager.hasAttachment(entry)) ? hasAttachmentImage : blankImage));
        item.add(new Image("hasSequence", (SequenceManager.hasSequence(entry)) ? hasSequenceImage
                : blankImage));
        item.add(new Image("hasSample",
                (AuthenticatedSampleManager.hasSample(entry)) ? hasSampleImage : blankImage));
    }

    protected void renderIndex(Item<T> item) {
        item.add(new Label("index", String.valueOf(getItemsPerPage() * getCurrentPage()
                + item.getIndex() + 1)));
    }

    protected void renderEntryLink(Item<T> item) {
        Entry entry = getEntry(item);

        BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>("partIdLink",
                EntryViewPage.class, new PageParameters("0=" + entry.getId()));

        entryLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
        String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
        entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));

        item.add(entryLink);
    }

    protected void renderCreationTime(Item<T> item) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

        String dateString = dateFormat.format(getEntry(item).getCreationTime());

        item.add(new Label("date", dateString));
    }
}
