package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.client.AppController;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnChangeUploaderHandler;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.OnStartUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

public class UploadSequenceFileWidget extends Composite {

    private DialogBox box;
    private Button saveButton;
    private Button cancelButton;
    private SingleUploader uploader;
    private final long entryId;
    private final Label feedback;
    private OnFinishUploaderHandler handler;

    public UploadSequenceFileWidget(long eid) {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");

        this.entryId = eid;
        initWidget(layout);
        initComponents();

        uploader = createUploader();
        layout.setWidget(0, 0, uploader);
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);

        feedback = new Label("");
        feedback.setStyleName("upload_sequence_feedback");
        layout.setWidget(1, 0, feedback);
        feedback.setVisible(false);

        layout.setWidget(2, 0, saveButton);
        layout.getCellFormatter().setHorizontalAlignment(2, 0, HasAlignment.ALIGN_RIGHT);
        layout.setWidget(2, 1, cancelButton);
        layout.getCellFormatter().setWidth(2, 1, "70px");
    }

    public void setFinishHandler(OnFinishUploaderHandler handler) {
        this.handler = handler;
    }

    private void initComponents() {

        // save cancel buttons
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                uploader.cancel();
                uploader.reset();
                box.hide();
            }
        });

        // dialog box
        box = new DialogBox();
        box.setWidth("600px");
        box.setModal(true);
        box.setHTML("Upload Sequence File");
        box.setGlassEnabled(true);
        box.setGlassStyleName("dialog_box_glass");
        box.setWidget(this);
    }

    public void showDialog() {
        feedback.setText("");
        feedback.setVisible(false);
        box.center();
    }

    public SingleUploader createUploader() {
        SingleUploader uploader = new SingleUploader(FileInputType.BROWSER_INPUT, null, saveButton) {

            @Override
            public Panel getUploaderPanel() {
                VerticalPanel vPanel = new VerticalPanel();
                vPanel.setWidth("180px");
                return vPanel;
            }
        };
        uploader.setAutoSubmit(false);
        saveButton.setText("Submit");

        uploader.addOnChangeUploadHandler(new OnChangeUploaderHandler() {

            @Override
            public void onChange(IUploader uploader) {
                feedback.setText("");
                feedback.setVisible(false);
            }
        });

        uploader.addOnStartUploadHandler(new OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                uploader.setServletPath("/sequence_upload?eid=" + entryId + "&type=file&sid="
                                                + AppController.sessionId);
            }
        });

        uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
            @Override
            public void onFinish(IUploader uploader) {
                if (uploader.getStatus() == Status.SUCCESS) {

                    UploadedInfo info = uploader.getServerInfo();
                    String errMsg = info.message;
                    if (errMsg.isEmpty()) {
                        feedback.setText(errMsg);
                        feedback.setVisible(false);
                        uploader.reset();
                        box.hide();

                        if (handler != null)
                            handler.onFinish(uploader);
                        return;
                    }

                    feedback.setText(errMsg);
                    feedback.setVisible(true);
                    uploader.reset();
                } else {
                    feedback.setText(
                            "You file could not be uploaded. Please try again. If this error persists, " +
                                    "please contact your administrator.");
                    feedback.setVisible(true);
                    uploader.reset();
                }
            }
        });
        return uploader;
    }
}
