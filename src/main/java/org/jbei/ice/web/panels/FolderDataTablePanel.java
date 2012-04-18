package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Folder;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.data.tables.ImageHeaderColumn;
import org.jbei.ice.web.data.tables.LabelHeaderColumn;
import org.jbei.ice.web.dataProviders.FolderDataProvider;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.utils.WebUtils;

public class FolderDataTablePanel extends SortableDataTablePanel<Entry> {

    private static final long serialVersionUID = 1L;

    public FolderDataTablePanel(String id, Folder folder) {
        super(id);

        dataProvider = new FolderDataProvider(folder);

        addIndexColumn();
        super.addTypeColumn("recordType", true);
        addPartIDColumn();
        super.addLabelHeaderColumn("Name", "oneName.name", "oneName.name");
        addSummaryColumn();
        addStatusColumn();
        addHasAttachmentColumn();
        addHasSampleColumn();
        addHasSequenceColumn();
        addCreationTimeColumn();

        this.setEntries(((FolderDataProvider) dataProvider).getEntries());

        renderTable();
    }

    protected void addIndexColumn() {
        addColumn(new LabelHeaderColumn<Entry>("#") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, final Entry entry, int index) {
                return new Label(componentId, String.valueOf((table.getCurrentPage() * table
                        .getRowsPerPage()) + index + 1));
            }
        });
    }

    protected void addHasAttachmentColumn() {
        addColumn(new ImageHeaderColumn<Entry>("has_attachment_fragment", "has_attachment",
                "attachment.gif", null, "Has Attachment", this) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String id, Entry entry, int row) {

                EntryController entryController = new EntryController(IceSession.get().getAccount());
                Fragment fragment = new Fragment(id, "has_attachment_fragment",
                        FolderDataTablePanel.this);

                try {
                    if (entryController.hasAttachments(entry))
                        fragment.add(new Image("has_attachment", hasAttachmentImage));
                    else
                        fragment.add(new Image("has_attachment", blankImage));
                } catch (ControllerException e) {
                    fragment.add(new Image("has_attachment", blankImage));
                }
                return fragment;
            }
        });
    }

    protected void addHasSampleColumn() {
        addColumn(new ImageHeaderColumn<Entry>("has_sample_fragment", "has_sample", "sample.png",
                null, "Has Samples", this) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String id, Entry entry, int row) {
                Fragment fragment = new Fragment(id, "has_sample_fragment",
                        FolderDataTablePanel.this);

                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    if (entryController.hasSamples(entry))
                        fragment.add(new Image("has_sample", hasSampleImage));
                    else
                        fragment.add(new Image("has_sample", blankImage));
                } catch (ControllerException e) {
                    fragment.add(new Image("has_sample", blankImage));
                }
                return fragment;
            }
        });
    }

    protected void addHasSequenceColumn() {

        addColumn(new ImageHeaderColumn<Entry>("has_sequence_fragment", "has_sequence",
                "sequence.gif", null, "Has Sequence", this) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String id, Entry entry, int row) {
                Fragment fragment = new Fragment(id, "has_sequence_fragment",
                        FolderDataTablePanel.this);

                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    if (entryController.hasSequence(entry))
                        fragment.add(new Image("has_sequence", hasSequenceImage));
                    else
                        fragment.add(new Image("has_sequence", blankImage));

                } catch (ControllerException e) {
                    fragment.add(new Image("has_sequence", blankImage));
                }
                return fragment;
            }
        });
    }

    protected void addPartIDColumn() {
        addColumn(new LabelHeaderColumn<Entry>("Part ID", "onePartNumber.partNumber") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Entry entry, int row) {
                Fragment fragment = new Fragment(componentId, "part_id_cell",
                        FolderDataTablePanel.this);

                BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>(
                        "partIdLink", EntryViewPage.class, new PageParameters("0=" + entry.getId()));

                entryLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
                fragment.add(entryLink);
                return fragment;
            }
        });
    }

    protected void addSummaryColumn() {
        addColumn(new LabelHeaderColumn<Entry>("Summary", null, null) {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Entry entry, int row) {
                String trimmedDescription = trimLongField(WebUtils.linkifyText(IceSession.get()
                        .getAccount(), entry.getShortDescription()), MAX_LONG_FIELD_LENGTH);
                return new Label(componentId, trimmedDescription).setEscapeModelStrings(false);
            }
        });
    }

    protected void addStatusColumn() {
        addColumn(new LabelHeaderColumn<Entry>("Status", "status") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Entry entry, int row) {
                return new Label(componentId, JbeiConstants.getStatus(entry.getStatus()));
            }
        });
    }

    protected void addCreationTimeColumn() {
        addColumn(new LabelHeaderColumn<Entry>("Created", "creationTime") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Entry entry, int row) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(entry.getCreationTime());
                return new Label(componentId, dateString);
            }
        });
    }
}
