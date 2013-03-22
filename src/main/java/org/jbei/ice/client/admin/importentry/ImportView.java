package org.jbei.ice.client.admin.importentry;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminTab;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.HasData;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

/**
 * @author Hector Plahar
 */
public class ImportView extends Composite implements AdminPanel<EntryInfo> {

    private final TextBox ownerName;
    private final TextBox ownerEmail;
    private final Button submit;
    private SingleUploader fileUploader;
    private final FlexTable layout;

    public ImportView() {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        initWidget(layout);

        // init
        ownerName = new TextBox();
        ownerName.setStyleName("input_box");
        ownerEmail = new TextBox();
        ownerEmail.setStyleName("input_box");
        submit = new Button("Submit");
        // file uploader

        fileUploader = new SingleUploader();
        fileUploader.setAutoSubmit(true);

        reset();
    }

    public void reset() {

        fileUploader.reset();

        layout.setHTML(0, 0, "File:");
        layout.setWidget(0, 1, fileUploader);
        layout.setHTML(1, 0, "Owner Name");
        layout.setWidget(1, 1, ownerName);
        layout.setHTML(2, 0, "Owner Email");
        layout.setWidget(2, 1, ownerEmail);
        layout.setWidget(3, 0, submit);
        layout.getFlexCellFormatter().setColSpan(3, 0, 2);
    }

    @Override
    public String getTabTitle() {
        return "Import";
    }

    @Override
    public HasData<EntryInfo> getDisplay() {
        return null;
    }

    @Override
    public AdminTab getTab() {
        return AdminTab.IMPORT;
    }

    public void setStartUploaderHandler(IUploader.OnStartUploaderHandler handler) {
        fileUploader.addOnStartUploadHandler(handler);
    }

    public void setFinishUploadHandler(IUploader.OnFinishUploaderHandler handler) {
        fileUploader.addOnFinishUploadHandler(handler);
    }

    public void setSubmitHandler(ClickHandler clickHandler) {
        this.submit.addClickHandler(clickHandler);
    }

    public boolean validates() {
        boolean validates = true;
        if (ownerName.getText().trim().isEmpty()) {
            ownerName.setStyleName("input_box_error");
            validates = false;
        } else {
            ownerName.setStyleName("input_box");
        }

        if (ownerEmail.getText().trim().isEmpty()) {
            ownerEmail.setStyleName("input_box_error");
            validates = false;
        } else {
            ownerEmail.setStyleName("input_box");
        }

        return validates;
    }

    public String getOwnerEmail() {
        return ownerEmail.getText();
    }

    public String getOwnerName() {
        return ownerName.getText();
    }

    public void setUploaded(String basename) {
        if (basename.isEmpty())
            return;

        layout.setHTML(0, 1, "<b>" + basename + "</b>");
    }


}
