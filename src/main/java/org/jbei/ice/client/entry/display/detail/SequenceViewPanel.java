package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.display.detail.SequenceViewPanelPresenter.ISequenceView;
import org.jbei.ice.client.entry.display.view.DeleteSequenceHandler;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
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
    private final PartData partData;
    private final FlexTable layout;
    private HTMLPanel headerPanel;
    private final SequenceViewPanelPresenter presenter;
    private DeleteSequenceHandler deleteHandler;
    private final boolean isEditMode;

    public SequenceViewPanel(PartData partData, boolean isEdit) {
        isEditMode = isEdit;
        this.partData = partData;
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        initWidget(layout);

        sequenceDownload = new SequenceFileDownload(partData.getId(), partData.isHasOriginalSequence());
        sequenceDownload.asWidget().addStyleName("display-inline");

        sequenceUpload = new SequenceFileUpload(partData.getId());
        sequenceUpload.asWidget().addStyleName("display-inline");

        layout.setWidget(0, 0, createSequenceHeader());
        layout.getFlexCellFormatter().setColSpan(0, 0, 6);

        layout.setWidget(1, 0, new Label(""));
        layout.getFlexCellFormatter().setHeight(1, 0, "10px");
        layout.getFlexCellFormatter().setColSpan(1, 0, 6);

        displaySBOLVisual();

        this.presenter = new SequenceViewPanelPresenter(this);
    }

    protected void displaySBOLVisual() {
        if (isEditMode) {
            layout.getFlexCellFormatter().setVisible(2, 0, false);
            Label upload = new Label("Upload");
            upload.setStyleName("footer_feedback_widget");
            upload.addStyleName("display-inline");

            Label paste = new Label("paste");
            paste.setStyleName("footer_feedback_widget");
            paste.addStyleName("display-inline");

            String html = "<span class=\"font-80em\" style=\"color: #999\"><span id=\"upload_link\"></span>"
                    + " or <span id=\"paste_sequence_link\"></span> sequence information</span>";
            HTMLPanel panel = new HTMLPanel(html);
            panel.add(upload, "upload_link");
            panel.add(paste, "paste_sequence_link");

            layout.setWidget(3, 0, panel);
            layout.getFlexCellFormatter().setHeight(3, 0, "20px");
            return;
        }

        String imgUrl = "";
        if (partData.isHasSequence() && partData.getSbolVisualURL() != null) {
            imgUrl = "<img height=\"170px\" src=\"/download?type=sbol_visual&id=" + partData.getSbolVisualURL()
                    + "\" /><br>";
        }

        ScrollPanel panel = new ScrollPanel();
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setWidth("100%");
        HTML html = new HTML(imgUrl);

        verticalPanel.add(html);
        panel.add(verticalPanel);
        layout.setWidget(2, 0, panel);
        layout.getFlexCellFormatter().setColSpan(2, 0, 6);

        updateSequenceContents();
        html.setWidth(layout.getOffsetWidth() + "px");
    }

    public SequenceViewPanel(PartData info) {
        this(info, false);
    }

    @Override
    public void setFinishHandler(OnFinishUploaderHandler handler) {
        sequenceUpload.setFileUploadWidgetFinishHandler(handler);
    }

    public void setDeleteHandler(DeleteSequenceHandler handler) {
        this.deleteHandler = handler;
        updateSequenceHeaders();
    }

    public PartData getPartData() {
        return this.partData;
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
        if (partData.isHasSequence()) {
            Flash.Parameters param = new Flash.Parameters();
            param.setEntryId(partData.getRecordId());
            param.setSessiondId(ClientController.sessionId);
            param.setSwfPath("vv/VectorViewer.swf");
            param.setMovieName("VectorViewer.swf");
            Flash flash = new Flash(param);
            layout.setWidget(3, 0, flash);
            layout.getFlexCellFormatter().setHeight(3, 0, "600px");
        } else {
            layout.getFlexCellFormatter().setVisible(2, 0, false);
            String html = "<span class=\"font-80em\"><i style=\"color: #999\">No sequence data provided</i></span>";
            layout.setHTML(3, 0, html);
            layout.getFlexCellFormatter().setHeight(3, 0, "20px");
        }
    }

    private Widget createSequenceHeader() {
        String html = "<span style=\"color: #233559; "
                + "font-weight: bold; font-style: italic; font-size: 0.80em;\">"
                + "SEQUENCE</span><div style=\"float: right\"><span id=\"delete_sequence_link\"></span>"
                + "<span id=\"sequence_link\"></span>"
                + "<span style=\"color: #262626; font-size: 0.75em;\">|</span>"
                + "<span id=\"sequence_options\"></span>";

        if (partData.isHasSequence()) {
            html += " <span style=\"color: #262626; font-size: 0.75em;\">|</span> <span id=\"sbol_visual\"></span>";
        }

        html += "</div>";

        headerPanel = new HTMLPanel(html);
        headerPanel.setStyleName("entry_sequence_sub_header");
        updateSequenceHeaders();
        return headerPanel;
    }

    @Override
    public void updateSequenceHeaders() {
        headerPanel.clear();

        if (partData.isHasSequence()) {
            // delete, open in vector editor, download
            Label label = new Label("Open");
            label.addClickHandler(new SequenceHeaderHandler());
            label.setStyleName("open_sequence_sub_link");
            headerPanel.add(label, "sequence_link");
            headerPanel.add(sequenceDownload.asWidget(), "sequence_options");
            if (deleteHandler != null)
                showSequenceDeleteLink(deleteHandler);

            // show sbol visual only if not in edit mode
            if (!isEditMode) {
                // sbol visual
                final Label sbVisual = new Label("Hide Pigeon Image");
                sbVisual.setStyleName("open_sequence_sub_link");
                sbVisual.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (layout.getFlexCellFormatter().isVisible(2, 0)) {
                            sbVisual.setText("Show Pigeon Image");
                            layout.getFlexCellFormatter().setVisible(2, 0, false);
                        } else {
                            sbVisual.setText("Hide Pigeon Image");
                            layout.getFlexCellFormatter().setVisible(2, 0, true);
                        }
                    }
                });
                headerPanel.add(sbVisual, "sbol_visual");
            }
        } else if (!isEditMode) {
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
        this.partData.setHasSequence(hasSequence);
        this.partData.setHasOriginalSequence(hasSequence);
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
            url += "static/swf/ve/VectorEditor.swf?entryId=" + partData.getRecordId() + "&sessionId="
                    + ClientController.sessionId;
            Window.open(url, partData.getName(), "");
        }
    }
}
