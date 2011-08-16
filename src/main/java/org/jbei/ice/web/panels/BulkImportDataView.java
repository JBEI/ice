package org.jbei.ice.web.panels;

import java.util.Date;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.jbei.ice.lib.managers.BulkImportManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.BulkImport;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.forms.JavascriptEventConfirmation;
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
        renderActionLink(item);
    }

    protected void renderOwnerName(Item<BulkImport> item) {
        BulkImport bulkImport = item.getModelObject();
        item.add(new Label("account", "" + bulkImport.getAccount().getFullName()));
    }

    protected void renderRecordType(Item<BulkImport> item) {
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

    protected void renderActionLink(final Item<BulkImport> item) {
        long id = item.getModel().getObject().getId();

        // edit link
        BookmarkablePageLink<AdminPage> editLink = new BookmarkablePageLink<AdminPage>("edit_link",
                AdminPage.class, new PageParameters("0=bulk_import,1=" + id));
        editLink.add(new Label("edit", "verify"));
        item.add(editLink);

        // delete link
        AjaxLink<Object> link = new AjaxLink<Object>("delete") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    BulkImportManager.delete(item.getModelObject());
                } catch (ManagerException e) {
                    throw new ViewException(e);
                }
                target.addComponent(BulkImportDataView.this.getParent());
            }
        };

        link.add(new JavascriptEventConfirmation("onclick", "Delete bulk import entry for \\'"
                + item.getModelObject().getAccount().getEmail() + "\\'?"));
        link.add(new Label("delete", "delete"));
        item.setOutputMarkupId(true);
        item.add(link);
    }
}
