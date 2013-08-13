package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.GenericPopup;
import org.jbei.ice.client.common.widget.ICanReset;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Widget for uploading a sequence file. It is intended to be displayed as a dialog
 *
 * @author Hector Plahar
 */
public class UploadSequenceFileWidget extends Composite implements ICanReset {

    private final long entryId;
    private Label feedback;
    private GenericPopup popup;
    private FlexTable layout;

    public UploadSequenceFileWidget(long eid) {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");

        this.entryId = eid;
        initWidget(layout);
        popup = new GenericPopup(this, "<b>Upload Sequence File</b>");

        initComponents();

        layout.setWidget(0, 0, feedback);
    }

    private void initComponents() {
        feedback = new Label("");
        feedback.setStyleName("upload_sequence_feedback");
        feedback.setVisible(false);

        final FormPanel formPanel = new FormPanel();
        formPanel.setAction("/upload?sid=" + ClientController.sessionId + "&type=sequence");
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);

        FileUpload fileUpload = new FileUpload();
        fileUpload.setName("uploadFormElement");
        formPanel.add(fileUpload);

        popup.addSaveButtonHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                formPanel.submit();
            }
        });

        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                formPanel.setAction(formPanel.getAction() + "&eid=" + entryId);
            }
        });

        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String errMsg = event.getResults();

                if (errMsg.isEmpty()) {
                    feedback.setText(errMsg);
                    feedback.setVisible(false);
                    formPanel.reset();
                    popup.hideDialog();
                    return;
                }

                feedback.setText(errMsg);
                feedback.setVisible(true);
            }
        });

        layout.setWidget(2, 0, formPanel);
    }

    public void showDialog() {
        feedback.setText("");
        feedback.setVisible(false);
        popup.showDialog();
    }

    @Override
    public void reset() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
