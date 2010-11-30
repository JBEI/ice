package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.FolderManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Folder;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.data.tables.ImageHeaderColumn;
import org.jbei.ice.web.data.tables.LabelHeaderColumn;
import org.jbei.ice.web.dataProviders.UserEntriesDataProvider;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.utils.WebUtils;

public class UserEntriesViewPanel extends SortableDataTablePanel<Entry> {
    private static final long serialVersionUID = 1L;

    public UserEntriesViewPanel(String id) {
        super(id);

        UserEntriesDataProvider provider = new UserEntriesDataProvider(IceSession.get()
                .getAccount());
        dataProvider = provider;

        // table columns
        // addDirectorySelectionColumn();
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

        setEntries(provider.getEntries());
        renderTable();
    }

    private static class DirectoryModel extends LoadableDetachableModel<List<Folder>> {

        private static final long serialVersionUID = 1L;

        @Override
        protected List<Folder> load() {
            try {
                return FolderManager.getFoldersByOwner(AccountManager.getSystemAccount());
            } catch (ManagerException e) {
                throw new ViewException(e);
            }
        }
    }

    protected void addDirectorySelectionColumn() {
        addColumn(new LabelHeaderColumn<Entry>("") {

            private static final long serialVersionUID = 1L;

            @Override
            public Component getHeader(String componentId) {
                Fragment fragment = new Fragment(componentId, "directory_header_fragment",
                        UserEntriesViewPanel.this);

                fragment.add(new AjaxCheckBox("select_all", new Model<Boolean>()) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        table.setSelectAllColumns(getModelObject().booleanValue());
                        target.addComponent(table);
                    }
                });

                // add list view for <li>
                ListView<Folder> list = new ListView<Folder>("directory", new DirectoryModel()) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void populateItem(ListItem<Folder> item) {
                        String label = item.getModelObject().getName();

                        AjaxCheckBox checkbox = new AjaxCheckBox("directory_selection",
                                new Model<Boolean>()) {

                            private static final long serialVersionUID = 1L;

                            @Override
                            protected void onUpdate(AjaxRequestTarget target) {
                                // TODO Auto-generated method stub
                            }
                        };

                        item.add(checkbox);
                        item.add(new Label("directory_label", label));
                    }
                };
                fragment.add(list);
                return fragment;
            }

            @Override
            protected Component evaluate(String componentId, final Entry entry, int row) {
                Fragment fragment = new Fragment(componentId, "checkbox_fragment",
                        UserEntriesViewPanel.this);

                Model<Boolean> model = new Model<Boolean>();
                Boolean value;
                if (table.isSelectAllColumns())
                    value = Boolean.TRUE;
                else {
                    if (table.isSelected(entry.getRecordId()))
                        value = Boolean.TRUE;
                    else
                        value = Boolean.FALSE;
                }
                model.setObject(value);

                fragment.add(new AjaxCheckBox("checkbox", model) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        String recordId = entry.getRecordId();
                        if (getModelObject().booleanValue())
                            table.addSelection(recordId);
                        else
                            table.removeSelection(recordId);
                    }
                });

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
                        UserEntriesViewPanel.this);

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
                String trimmedDescription = trimLongField(
                    WebUtils.linkifyText(entry.getShortDescription()), MAX_LONG_FIELD_LENGTH);
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

    protected void addHasAttachmentColumn() {
        addColumn(new ImageHeaderColumn<Entry>("has_attachment_fragment", "has_attachment",
                "attachment.gif", null, "Has Attachment", this) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String id, Entry entry, int row) {

                EntryController entryController = new EntryController(IceSession.get().getAccount());
                Fragment fragment = new Fragment(id, "has_attachment_fragment",
                        UserEntriesViewPanel.this);

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
                        UserEntriesViewPanel.this);

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
                        UserEntriesViewPanel.this);

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