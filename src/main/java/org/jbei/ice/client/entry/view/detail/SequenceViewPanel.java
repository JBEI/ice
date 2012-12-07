package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter.ISequenceView;
import org.jbei.ice.client.entry.view.view.DeleteSequenceHandler;
import org.jbei.ice.shared.dto.entry.EntryInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import gwtupload.client.IUploader.OnFinishUploaderHandler;

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
    private final SequenceViewPanelPresenter presenter;
    private DeleteSequenceHandler deleteHandler;

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

    @Override
    public void setFinishHandler(OnFinishUploaderHandler handler) {
        sequenceUpload.setFileUploadWidgetFinishHandler(handler);
    }

    public void setDeleteHandler(DeleteSequenceHandler handler) {
        this.deleteHandler = handler;
        updateSequenceHeaders();
    }

    public EntryInfo getInfo() {
        return this.info;
    }

    @Override
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
            param.setSessiondId(ClientController.sessionId);
            param.setSwfPath("vv/VectorViewer.swf");
            param.setMovieName("VectorViewer.swf");
            layout.setWidget(2, 0, new Flash(param));
            layout.getFlexCellFormatter().setHeight(2, 0, "600px");
        } else {
            layout.setHTML(2, 0, "<span class=\"font-80em\"><i>No sequence provided</i></span>");
            layout.getFlexCellFormatter().setHeight(2, 0, "20px");
        }
    }

    private Widget createSequenceHeader() {
        headerPanel = new HTMLPanel(
                "<span style=\"color: #233559; "
                        + "font-weight: bold; font-style: italic; font-size: 0.80em;\">"
                        + "SEQUENCE</span><div style=\"float: right\"><span id=\"delete_sequence_link\"></span><span " +
                        "id=\"sequence_link\"></span>"
                        + "<span style=\"color: #262626; font-size: 0.75em;\">|</span>"
                        + " <span id=\"sequence_options\"></span></div>");

        headerPanel.setStyleName("entry_sequence_sub_header");
        updateSequenceHeaders();
        return headerPanel;
    }

    @Override
    public void updateSequenceHeaders() {
        headerPanel.clear();

        if (info.isHasSequence()) {
            // delete, open in vector editor, download
            Label label = new Label("Open");
            label.addClickHandler(new SequenceHeaderHandler());
            label.setStyleName("open_sequence_sub_link");
            headerPanel.add(label, "sequence_link");
            headerPanel.add(sequenceDownload.asWidget(), "sequence_options");
            if (deleteHandler != null)
                showSequenceDeleteLink(deleteHandler);
        } else {
            Label label = new Label("Create New");
            label.addClickHandler(new SequenceHeaderHandler());
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

    @Override
    public void showSequenceDeleteLink(DeleteSequenceHandler deleteHandler) {
        // owners and admins are the only ones that can edit
        if (presenter != null && presenter.isCanEdit()) {
            DeleteSequenceData delete = new DeleteSequenceData(presenter, deleteHandler);
            headerPanel.add(delete.getLabelWidget(), "delete_sequence_link");
        }
    }

    private class SequenceHeaderHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            String url = GWT.getHostPageBaseURL();
            url += "static/swf/ve/VectorEditor.swf?entryId=" + info.getRecordId() + "&sessionId="
                    + ClientController.sessionId;
            Window.open(url, info.getName(), "");
        }
    }
}
