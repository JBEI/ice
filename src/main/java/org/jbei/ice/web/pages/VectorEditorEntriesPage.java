package org.jbei.ice.web.pages;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;

public class VectorEditorEntriesPage extends WebPage {
    // Constructor
    public VectorEditorEntriesPage() {
        super();

        try {
            PageableListView listView = new PageableListView("itemRows", retrieveEntries(), 15) {
                private static final long serialVersionUID = -3505764282126141702L;

                @Override
                protected void populateItem(ListItem item) {
                    Entry entry = (Entry) item.getModelObject();

                    item.add(new Label("id", String.valueOf(entry.getId())));
                    item.add(new Label("partNumber",
                            entry.getPartNumbers().iterator().hasNext() ? entry.getPartNumbers()
                                    .iterator().next().getPartNumber() : ""));
                    item.add(new Label("name", entry.getNames().iterator().hasNext() ? entry
                            .getNames().iterator().next().getName() : ""));
                    item.add(new Label("owner", entry.getOwner()));
                    item.add(new Label("type", entry.getRecordType()));
                    item.add(new Label("summary", entry.getShortDescription()));

                    Format dateFormatter = new SimpleDateFormat("dd MMM yyyy");
                    String created = entry.getCreationTime() == null ? "" : dateFormatter
                            .format(entry.getCreationTime());
                    item.add(new Label("created", created));

                    PageParameters parameters = new PageParameters();
                    parameters.add("entryId", entry.getRecordId());

                    item.add(new BookmarkablePageLink<VectorEditorPage>("viewLink",
                            VectorEditorPage.class, parameters));
                }
            };

            add(listView);
        } catch (Exception e) {
            System.out.print(e.toString());
        }
    }

    // Private Methods
    private List<Entry> retrieveEntries() throws Exception {
        try {
            List<Sequence> sequences = null;

            sequences = SequenceManager.getAll();

            List<Entry> entries = new ArrayList<Entry>();

            Iterator<Sequence> sequenceIterator = sequences.iterator();

            while (sequenceIterator.hasNext()) {
                entries.add(sequenceIterator.next().getEntry());
            }

            return entries;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());

            throw new Exception(e);
        }
    }
}
