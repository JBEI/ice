package org.jbei.ice.client.entry.display.panel;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.display.view.SequenceAnalysisHeaderPanel;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 * Panel that handles display and upload of trace sequence files
 *
 * @author Hector Plahar
 */
public class EntrySequenceAnalysisPanel extends Composite {

    private final FlexTable layout;
    private final EntrySequenceTable sequenceTable;
    private final SequenceAnalysisHeaderPanel traceHeaderPanel;
    private Widget uploadPanel;
    private PartData currentInfo;
    private final Delegate<Long> retrieveSequenceTracesDelegate;

    public EntrySequenceAnalysisPanel(Delegate<Long> retrieveSequenceTracesDelegate) {
        this.retrieveSequenceTracesDelegate = retrieveSequenceTracesDelegate;
        layout = new FlexTable();
        initWidget(layout);

        sequenceTable = new EntrySequenceTable();
        traceHeaderPanel = new SequenceAnalysisHeaderPanel(sequenceTable.getSelectionModel());
        uploadPanel = createSequenceUploadPanel();

        traceHeaderPanel.setTraceUploadHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                layout.getRowFormatter().setVisible(1, !layout.getRowFormatter().isVisible(1));
            }
        });

        initLayout();
    }

    protected void initLayout() {
        layout.clear();
        layout.setWidth("100%");
        layout.setWidget(0, 0, traceHeaderPanel);
        layout.setWidget(1, 0, uploadPanel);
        layout.setHTML(2, 0, "<i class=\"font-75em\" style=\"color: #999\">No sequence trace files available.</i>");
        layout.getRowFormatter().setVisible(1, false);
    }

    public void setCurrentInfo(PartData info) {
        this.currentInfo = info;
        if (info == null)
            return;

        traceHeaderPanel.setUserCanEdit(info.isCanEdit());
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
            layout.setHTML(2, 0, "<i class=\"font-75em\" style=\"color: #999\">No sequence trace files available</i>");
            if (layout.getRowCount() > 3) {
                for (int j = 3; j < layout.getRowCount(); j += 1)
                    layout.removeRow(j);
            }
        }
    }

    public void setTraceSequenceDeleteHandler(ClickHandler handler) {
        traceHeaderPanel.setDeleteHandler(handler);
    }

    public void reset() {
        sequenceTable.reset();
        currentInfo = null;
        initLayout();
    }

    private Widget createSequenceUploadPanel() {
        final FormPanel formPanel = new FormPanel();
        formPanel.setAction("/upload?sid=" + ClientController.sessionId + "&type=trace_sequence");
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);

        final FileUpload sequenceUploader = new FileUpload();
        sequenceUploader.setName("uploadFormElement");
        formPanel.add(sequenceUploader);

        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                if (formPanel.getAction().contains("eid="))
                    return;
                formPanel.setAction(formPanel.getAction() + "&eid=" + currentInfo.getId());
            }
        });

        sequenceUploader.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (sequenceUploader.getFilename().isEmpty())
                    return;

                formPanel.submit();
            }
        });

        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                retrieveSequenceTracesDelegate.execute(currentInfo.getId());
                formPanel.reset();
                layout.getRowFormatter().setVisible(1, false);
            }
        });

        HTML html = new HTML("Upload Fasta, GenBank, or ABI formats, optionally in a zip file");
        html.setStyleName("information");

        VerticalPanel panel = new VerticalPanel();
        panel.add(html);
        panel.add(formPanel);
        return panel;
    }
}
