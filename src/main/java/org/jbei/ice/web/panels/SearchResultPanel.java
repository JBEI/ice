package org.jbei.ice.web.panels;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.SearchResult;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class SearchResultPanel extends Panel {
    private static final long serialVersionUID = 1L;

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

        @SuppressWarnings("unchecked")
        PageableListView listView = new PageableListView("itemRows", searchResults, limit) {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            protected void populateItem(ListItem item) {
                SearchResult searchResult = (SearchResult) item.getModelObject();
                Entry entry = searchResult.getEntry();

                item.add(new Label("index", "" + (item.getIndex() + 1)));
                item.add(new Label("recordType", entry.getRecordType()));
                BookmarkablePageLink partIdLink = new BookmarkablePageLink("partIdLink",
                        EntryViewPage.class, new PageParameters("0=" + entry.getId()));
                partIdLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                partIdLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
                item.add(partIdLink);
                item.add(new Label("name", entry.getOneName().getName()));
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

                NumberFormat formatter = new DecimalFormat("##");
                String scoreString = formatter.format(searchResult.getScore() * 100);
                item.add(new Label("score", scoreString));

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

        add(listView);
        add(new JbeiPagingNavigator("navigator", listView));
    }
}
