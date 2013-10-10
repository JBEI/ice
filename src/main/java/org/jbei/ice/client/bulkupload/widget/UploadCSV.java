package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.Dialog;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.EntryAddType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Widget to enable upload of csv files. Places a "CSV Upload" label
 * which when clicked, shows a popup that the user can interact with to upload the csv file
 *
 * @author Hector Plahar
 */
public class UploadCSV extends Composite {

    private HTML label;
    private HandlerRegistration handler;
    private Dialog dialog;
    private EntryAddType addType;
    private ServiceDelegate<Long> delegate;

    public UploadCSV() {
        label = new HTML("<i class=\"" + FAIconType.CLOUD_UPLOAD.getStyleName() + "\"></i> CSV Upload");
        label.setStyleName("bulk_upload_visibility");
        label.addStyleName("opacity_hover");
        initWidget(label);

        initFileUpload();
        addHandlers();
    }

    public void setAddType(EntryAddType addType) {
        this.addType = addType;
    }

    protected void addHandlers() {
        if (handler != null)
            handler.removeHandler();
        handler = label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.showDialog(true);
            }
        });
    }

    protected void initFileUpload() {
        final FlexTable table = new FlexTable();

        final FormPanel formPanel = new FormPanel();
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);

        final FileUpload fileUpload = new FileUpload();
        fileUpload.setName("uploadFormElement");
        formPanel.add(fileUpload);

        table.setWidget(0, 0, formPanel);
        table.setHTML(1, 0, "");
        table.getFlexCellFormatter().setVisible(1, 0, false);

        dialog = new Dialog(table, "600px", "CSV Upload");

        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String msg = event.getResults();
                table.setHTML(1, 0, msg);
                boolean success = !msg.startsWith("Error");
                table.getFlexCellFormatter().setVisible(1, 0, !success);
                if (success) {
                    try {
                        delegate.execute(Long.decode(msg.trim()));
                        dialog.showDialog(false);
                    } catch (NumberFormatException nfe) {
                        table.setHTML(1, 0, "There was an unknown problem creating the parts from the file");
                        table.getFlexCellFormatter().setVisible(1, 0, true);
                    }
                }

                formPanel.reset();
            }
        });

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fileUpload.getFilename().isEmpty())
                    return;

                formPanel.setAction(
                        "/upload?sid=" + ClientController.sessionId + "&type=bulk_csv&upload=" + addType.name());
                formPanel.submit();
            }
        });
    }

    public void setDelegate(ServiceDelegate<Long> handler) {
        delegate = handler;
    }
}
