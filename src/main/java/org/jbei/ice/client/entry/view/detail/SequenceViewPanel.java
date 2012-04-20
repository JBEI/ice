package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter.ISequenceView;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that displays the sequence file on the general view
 * 
 * @author Hector Plahar
 */
public class SequenceViewPanel extends Composite implements ISequenceView {

    private final SequenceFileDownload sequenceDownload;
    private final SequenceFileUpload sequenceUpload;
    private final EntryInfo info;
    private final FlexTable layout;
    private HTMLPanel headerPanel;
    private VectorEditorDialog dialog;
    private final SequenceViewPanelPresenter presenter;

    public SequenceViewPanel(EntryInfo info) {
        this.info = info;
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        initWidget(layout);

        sequenceDownload = new SequenceFileDownload(info.getId());
        sequenceDownload.asWidget().addStyleName("display-inline");

        sequenceUpload = new SequenceFileUpload(info.getId());
        sequenceUpload.asWidget().addStyleName("display-inline");

        layout.setWidget(0, 0, createSequenceHeader());
        layout.getFlexCellFormatter().setColSpan(0, 0, 6);

        layout.setWidget(1, 0, new Label(""));
        layout.getFlexCellFormatter().setHeight(1, 0, "10px");
        layout.getFlexCellFormatter().setColSpan(1, 0, 6);
        updateSequenceContents();
        this.presenter = new SequenceViewPanelPresenter(this);
    }

    public EntryInfo getInfo() {
        return this.info;
    }

    public String getSequence() {
        return sequenceUpload.getPastedSequence();
    }

    public SequenceViewPanelPresenter getPresenter() {
        return this.presenter;
    }

    @Override
    public void updateSequenceContents() {
        // check if there is a sequence 
        if (info.isHasSequence()) {
            Flash.Parameters param = new Flash.Parameters();
            param.setEntryId(info.getRecordId());
            param.setSessiondId(AppController.sessionId);
            param.setSwfPath("vv/VectorViewer.swf");
            layout.setWidget(2, 0, new Flash(param));
            layout.getFlexCellFormatter().setHeight(2, 0, "600px");
        } else {
            layout.setHTML(2, 0, "<span class=\"font-80em\">No sequence provided.</span>");
        }
    }

    private Widget createSequenceHeader() {
        headerPanel = new HTMLPanel("<span style=\"color: #233559; "
                + "font-weight: bold; font-style: italic; font-size: 0.80em;\">"
                + "SEQUENCE</span><div style=\"float: right\"><span id=\"sequence_link\"></span>"
                + "<span style=\"color: #262626; font-size: 0.75em;\">|</span>"
                + " <span id=\"sequence_options\"></span></div>");

        headerPanel.setStyleName("entry_sequence_sub_header");

        dialog = new VectorEditorDialog(info.getName());
        Flash.Parameters param = new Flash.Parameters();
        param.setEntryId(info.getRecordId());
        param.setSessiondId(AppController.sessionId);
        param.setSwfPath("ve/VectorEditor.swf");

        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setHeight("100%");
        table.setWidget(0, 0, new Flash(param));
        table.getFlexCellFormatter().setHeight(0, 0, "100%");
        dialog.setWidget(table);
        updateSequenceHeaders();

        return headerPanel;
    }

    @Override
    public void updateSequenceHeaders() {
        headerPanel.clear();

        if (info.isHasSequence()) {
            // delete, open in vector editor, download
            Label label = dialog.getLabel("Open");
            label.setStyleName("open_sequence_sub_link");
            headerPanel.add(label, "sequence_link");
            headerPanel.add(sequenceDownload.asWidget(), "sequence_options");

            // TODO : delete
        } else {
            Label label = dialog.getLabel("Create New");
            label.setStyleName("open_sequence_sub_link");
            headerPanel.add(label, "sequence_link");
            headerPanel.add(sequenceUpload.asWidget(), "sequence_options");
        }
    }

    @Override
    public void addSubmitSequencePasteHandler(ClickHandler handler) {
        sequenceUpload.addSubmitSequencePasteHandler(handler);
    }

    @Override
    public void setHasSequence(boolean hasSequence) {
        this.info.setHasSequence(hasSequence);
    }

    @Override
    public void hideDialog() {
        sequenceUpload.hidePasteDialog();
    }
}
