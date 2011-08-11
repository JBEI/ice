package org.jbei.ice.web.panels;

import java.util.Date;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.jbei.ice.lib.models.BulkImport;
import org.jbei.ice.web.pages.AdminPage;

import com.ibm.icu.text.DateFormat;

public class BulkImportDataView extends DataView<BulkImport> {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_RECORD_COUNT = 50;

    public BulkImportDataView(String id, IDataProvider<BulkImport> dataProvider, ModalWindow window) {
        super(id, dataProvider, PAGE_RECORD_COUNT);
    }

    @Override
    protected void populateItem(Item<BulkImport> item) {

        renderOwnerName(item);
        renderOwnerEmail(item);
        renderRecordType(item);
        renderRecordCount(item);
        renderCreated(item);
        renderEditLink(item);
    }

    protected void renderOwnerName(Item<BulkImport> item) {
        BulkImport bulkImport = item.getModelObject();
        item.add(new Label("account", "" + bulkImport.getAccount().getFullName()));
    }

    protected void renderRecordType(Item<BulkImport> item) {
        //        List<BulkImportEntryData> bulkImport = item.getModelObject().getPrimaryData();
        //        Label label;
        //        if (bulkImport == null || bulkImport.isEmpty())
        //            label = new Label("record_type", "none");
        //        else {
        //            BulkImportEntryData data = bulkImport.get(0);
        //            Entry entry = data.getEntry();
        //            label = new Label("record_type", entry.getRecordType());
        //        }

        BulkImport bulkImport = item.getModelObject();
        Label label;
        if (bulkImport == null)
            label = new Label("record_type", "");
        else
            label = new Label("record_type", bulkImport.getType());
        item.add(label);
    }

    protected void renderRecordCount(Item<BulkImport> item) {
        int recordCount = item.getModel().getObject().getPrimaryData().size();
        item.add(new Label("record_count", "" + recordCount));
    }

    protected void renderOwnerEmail(Item<BulkImport> item) {
        String email = item.getModel().getObject().getAccount().getEmail();
        item.add(new Label("email", email));
    }

    protected void renderCreated(Item<BulkImport> item) {
        Date created = item.getModel().getObject().getCreationTime();
        String createdStr = DateFormat.getDateInstance(DateFormat.MEDIUM).format(created);
        item.add(new Label("created", createdStr));
    }

    protected void renderEditLink(Item<BulkImport> item) {
        long id = item.getModel().getObject().getId();
        BookmarkablePageLink<AdminPage> editLink = new BookmarkablePageLink<AdminPage>("edit_link",
                AdminPage.class, new PageParameters("0=bulk_import,1=" + id));
        editLink.add(new Label("edit", "verify"));
        item.add(editLink);
    }
}
