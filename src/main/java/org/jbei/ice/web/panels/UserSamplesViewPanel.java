package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.data.tables.LabelHeaderColumn;
import org.jbei.ice.web.dataProviders.UserSamplesDataProvider;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.StoragePage;
import org.jbei.ice.web.utils.WebUtils;

public class UserSamplesViewPanel extends SortableDataTablePanel<Sample> {
    private static final long serialVersionUID = 1L;

    public UserSamplesViewPanel(String id) {
        super(id);

        dataProvider = new UserSamplesDataProvider(IceSession.get().getAccount());

        addColumns();
        renderTable();
    }

    protected void addColumns() {
        super.addIndexColumn();
        super.addTypeColumn("entry.recordType", true);
        addPartIDColumn();
        super.addLabelHeaderColumn("Name", "entry.oneName.name", "entry.oneName.name");
        super.addLabelHeaderColumn("Label", "label", "label");
        addNotesColumn();
        addLocationColumn();
        addCreationTimeColumn();
    }

    protected void addCreationTimeColumn() {
        addColumn(new LabelHeaderColumn<Sample>("Created", "creationTime") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Sample sample, int row) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(sample.getCreationTime());
                return new Label(componentId, dateString);
            }
        });
    }

    protected void addLocationColumn() {
        addColumn(new LabelHeaderColumn<Sample>("Location") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Sample sample, int row) {
                Fragment fragment = new Fragment(componentId, "location_id_cell",
                        UserSamplesViewPanel.this);

                BookmarkablePageLink<StoragePage> storagePageLink = new BookmarkablePageLink<StoragePage>(
                        "storageLink", StoragePage.class, new PageParameters("0="
                                + sample.getStorage().getId()));
                storagePageLink.add(new Label("storageLinkLabel", sample.getStorage().toString()));
                fragment.add(storagePageLink);
                return fragment;
            }
        });
    }

    protected void addNotesColumn() {

        addColumn(new LabelHeaderColumn<Sample>("Notes") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Sample sample, int row) {
                return new Label(componentId, WebUtils.linkifyText(sample.getNotes()))
                        .setEscapeModelStrings(false);
            }
        });
    }

    protected void addPartIDColumn() {
        addColumn(new LabelHeaderColumn<Sample>("Part ID", "entry.onePartNumber.partNumber") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Sample sample, int row) {
                Fragment fragment = new Fragment(componentId, "part_id_cell",
                        UserSamplesViewPanel.this);

                BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>(
                        "partIdLink", EntryViewPage.class, new PageParameters("0="
                                + sample.getEntry().getId()));

                entryLink.add(new Label("partNumber", sample.getEntry().getOnePartNumber()
                        .getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/"
                        + sample.getEntry().getId()));
                fragment.add(entryLink);
                return fragment;
            }
        });
    }
}
