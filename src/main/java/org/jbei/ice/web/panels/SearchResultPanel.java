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
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.SearchResult;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.UserPage;

public class SearchResultPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public SearchResultPanel(String id, ArrayList<SearchResult> searchResults, int limit) {
        super(id);

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
                item.add(new Label("owner", (entry.getOwner() != null) ? entry.getOwner() : entry
                        .getOwnerEmail()));

                NumberFormat formatter = new DecimalFormat("##");
                String scoreString = formatter.format(searchResult.getScore() * 100);
                item.add(new Label("score", scoreString));

                ResourceReference blankImage = new ResourceReference(SearchResultPanel.class,
                        "blank.png");
                ResourceReference hasAttachmentImage = new ResourceReference(
                        SearchResultPanel.class, "attachment.gif");
                ResourceReference hasSequenceImage = new ResourceReference(SearchResultPanel.class,
                        "sequence.gif");
                ResourceReference hasSampleImage = new ResourceReference(SearchResultPanel.class,
                        "sample.png");

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

                add(JavascriptPackageResource.getHeaderContribution(UserPage.class,
                        "jquery-ui-1.7.2.custom.min.js"));
                add(JavascriptPackageResource.getHeaderContribution(UserPage.class,
                        "jquery.cluetip.js"));
                add(CSSPackageResource.getHeaderContribution(UserPage.class, "jquery.cluetip.css"));
            }
        };

        add(listView);
        add(new JbeiPagingNavigator("navigator", listView));
    }
}
