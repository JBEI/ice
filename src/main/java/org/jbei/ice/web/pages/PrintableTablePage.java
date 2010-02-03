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
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeiConstants;

public class PrintableTablePage extends ProtectedPage {
    public PrintableTablePage(ArrayList<Entry> entries) {
        super();

        add(new ListView<Entry>("entriesDataView", entries) {
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
                item.add(new Label("status", JbeiConstants.getStatus(entry.getStatus())));

                add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery-ui-1.7.2.custom.min.js"));
                add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery.cluetip.js"));
                add(CSSPackageResource.getHeaderContribution(EntryNewPage.class,
                        "jquery.cluetip.css"));

                ResourceReference blankImage = new ResourceReference(PrintableTablePage.class,
                        "blank.png");
                ResourceReference hasAttachmentImage = new ResourceReference(
                        PrintableTablePage.class, "attachment.gif");
                ResourceReference hasSequenceImage = new ResourceReference(
                        PrintableTablePage.class, "sequence.gif");
                ResourceReference hasSampleImage = new ResourceReference(PrintableTablePage.class,
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
            }
        });
    }

    @Override
    protected void initializeComponents() {
        add(new Label("title", "Printable"));
    }
}
