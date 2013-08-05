package org.jbei.ice.client.entry.display.panel;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.display.view.SequenceAnalysisHeaderPanel;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

/**
 * @author Hector Plahar
 */
public class EntrySequenceAnalysisPanel extends Composite {

    private final FlexTable layout;
    private final EntrySequenceTable sequenceTable;
    private final SequenceAnalysisHeaderPanel traceHeaderPanel;
    private SingleUploader sequenceUploader;
    private HTML sequenceAddCancelButton;
    private Widget uploadPanel;
    private HandlerRegistration sequenceUploadFinish;
    private PartData currentInfo;
    private final Delegate<Long> retrieveSequenceTracesDelegate;

    public EntrySequenceAnalysisPanel(Delegate<Long> retrieveSequenceTracesDelegate) {
        this.retrieveSequenceTracesDelegate = retrieveSequenceTracesDelegate;
        layout = new FlexTable();
        initWidget(layout);

        sequenceTable = new EntrySequenceTable();
        traceHeaderPanel = new SequenceAnalysisHeaderPanel(sequenceTable.getSelectionModel());

        uploadPanel = createSequenceUploadPanel();
        uploadPanel.setVisible(false);

        sequenceAddCancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                uploadPanel.setVisible(false);
            }
        });

        traceHeaderPanel.setTraceUploadHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                uploadPanel.setVisible(!uploadPanel.isVisible());
            }
        });

        sequenceUploader.addOnStartUploadHandler(new IUploader.OnStartUploaderHandler() {
            @Override
            public void onStart(IUploader uploader) {
                String servletPath = "servlet.gupld?eid=" + currentInfo.getId()
                        + "&type=sequence&sid=" + ClientController.sessionId;
                uploader.setServletPath(servletPath);
            }
        });

        initLayout();
        setSequenceFinishUploadHandler();
    }

    protected void initLayout() {
        layout.clear();
        layout.setWidth("100%");
        layout.setWidget(0, 0, traceHeaderPanel);
        layout.setWidget(1, 0, uploadPanel);
        layout.setHTML(2, 0, "<i class=\"font-75em\">No sequence trace file available.</i>");
    }

    public void setCurrentInfo(PartData info) {
        this.currentInfo = info;
        if (info == null)
            return;

        boolean hasSequence = info.isHasSequence();
        boolean hasSequenceFiles = info.getSequenceAnalysis() != null && !info.getSequenceAnalysis().isEmpty();

        if (hasSequence && hasSequenceFiles) {
            Flash.Parameters params = new Flash.Parameters();
            params.setSwfPath("sc/SequenceChecker.swf");
            params.setSessiondId(ClientController.sessionId);
            params.setMovieName("SequenceChecker.swf");
            params.setEntryId(info.getRecordId());

            Flash flash = new Flash(params);
            layout.setWidget(3, 0, flash);
            layout.getFlexCellFormatter().setHeight(3, 0, "600px");
        } else if (layout.getRowCount() > 3) {
            for (int j = 3; j < layout.getRowCount(); j += 1)
                layout.removeRow(j);
        }
    }

    public MultiSelectionModel<SequenceAnalysisInfo> getSelectionModel() {
        return sequenceTable.getSelectionModel();
    }

    public void setSequenceData(ArrayList<SequenceAnalysisInfo> data, PartData info) {
        sequenceTable.setData(data);
        if (data != null && !data.isEmpty()) {
            layout.setWidget(2, 0, sequenceTable);
            if (info.isHasSequence()) {
                Flash.Parameters params = new Flash.Parameters();
                params.setSwfPath("sc/SequenceChecker.swf");
                params.setSessiondId(ClientController.sessionId);
                params.setMovieName("SequenceChecker.swf");
                params.setEntryId(info.getRecordId());

                Flash flash = new Flash(params);
                layout.setWidget(3, 0, flash);
                layout.getFlexCellFormatter().setHeight(3, 0, "600px");
            }
        } else {
            layout.setHTML(2, 0, "<i class=\"font-75em\">No sequence trace file available</i>");
            if (layout.getRowCount() > 3) {
                for (int j = 3; j < layout.getRowCount(); j += 1)
                    layout.removeRow(j);
            }
        }
    }

    public void setTraceSequenceDeleteHandler(ClickHandler handler) {
        traceHeaderPanel.setDeleteHandler(handler);
    }

    protected void setSequenceFinishUploadHandler() {
        if (sequenceUploadFinish != null)
            sequenceUploadFinish.removeHandler();

        sequenceUploadFinish = sequenceUploader.addOnFinishUploadHandler(new IUploader.OnFinishUploaderHandler() {
            @Override
            public void onFinish(IUploader uploader) {
                if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                    IUploader.UploadedInfo info = uploader.getServerInfo();
                    if (!info.message.isEmpty())
                        Window.alert(info.message);
                    else
                        retrieveSequenceTracesDelegate.execute(currentInfo.getId());
                } else {
                    IUploader.UploadedInfo info = uploader.getServerInfo();
                    if (uploader.getStatus() == IUploadStatus.Status.ERROR) {
                        Window.alert("There was a problem uploading your file.\n\n"
                                             + "Please contact your administrator if this problem persists");
                    } else if (!info.message.isEmpty())
                        Window.alert(info.message);
                }
                uploader.reset();
                uploadPanel.setVisible(false);
            }
        });
    }

    public void reset() {
        sequenceTable.reset();
        currentInfo = null;
        initLayout();
    }

    private Widget createSequenceUploadPanel() {
        sequenceUploader = new SingleUploader();
        sequenceUploader.setAutoSubmit(true);
        sequenceUploader.addOnStartUploadHandler(new IUploader.OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                uploader.setServletPath(uploader.getServletPath() + "?eid=" + currentInfo.getId()
                                                + "&type=sequence&sid=" + ClientController.sessionId);
            }
        });

        sequenceAddCancelButton = new HTML("Cancel");
        sequenceAddCancelButton.setStyleName("footer_feedback_widget");
        sequenceAddCancelButton.addStyleName("font-70em");
        sequenceAddCancelButton.addStyleName("display-inline");

        String html = "<div style=\"outline:none; padding: 4px; background-color: #f3f3f3\">"
                + "<span id=\"upload\"></span><span style=\"color: #777; font-size: 10px;\">"
                + "Fasta, GenBank, or ABI formats, optionally in zip file.</span>"
                + "<span style=\"padding-left: 20px;\" id=\"upload_cancel\"></span></div>";

        HTMLPanel panel = new HTMLPanel(html);
        panel.add(sequenceUploader, "upload");
        panel.add(sequenceAddCancelButton, "upload_cancel");
        return panel;
    }
}
