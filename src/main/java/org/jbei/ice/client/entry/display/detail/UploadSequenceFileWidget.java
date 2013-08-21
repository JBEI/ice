package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.GenericPopup;
import org.jbei.ice.client.common.widget.ICanReset;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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

    private Label feedback;
    private GenericPopup popup;
    private FlexTable layout;
    private FormPanel formPanel;
    private String fileName;

    public UploadSequenceFileWidget() {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");

        initWidget(layout);
        popup = new GenericPopup(this, "<b>Upload Sequence File</b>");

        initComponents();

        layout.setWidget(0, 0, feedback);
    }

    private void initComponents() {
        feedback = new Label("");
        feedback.setStyleName("upload_sequence_feedback");
        feedback.setVisible(false);

        formPanel = new FormPanel();
        formPanel.setAction("/upload?sid=" + ClientController.sessionId + "&type=sequence");
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);

        final FileUpload fileUpload = new FileUpload();
        fileUpload.setName("uploadFormElement");
        formPanel.add(fileUpload);

        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String msg = event.getResults();
                if (msg.contains("Error")) {
                    feedback.setText(msg);
                    feedback.setVisible(true);
                    return;
                }

                fileName = msg;
            }
        });

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fileUpload.getFilename().isEmpty())
                    return;

                formPanel.submit();
            }
        });

        layout.setWidget(2, 0, formPanel);
    }

    public void addSaveHandler(ClickHandler handler) {
        popup.addSaveButtonHandler(handler);
        popup.hideDialog();
    }

    public void showDialog() {
        feedback.setText("");
        feedback.setVisible(false);
        popup.showDialog();
    }

    public void hideDialog() {
        popup.hideDialog();
    }

    public String getFileName() {
        return this.fileName;
    }

    @Override
    public void reset() {
        fileName = null;
        feedback.setText("");
        feedback.setVisible(false);
        formPanel.reset();
    }
}
