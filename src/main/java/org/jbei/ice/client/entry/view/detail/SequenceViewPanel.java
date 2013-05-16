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
    private final EntryInfo info;
    private final FlexTable layout;
    private HTMLPanel headerPanel;
    private final SequenceViewPanelPresenter presenter;
    private DeleteSequenceHandler deleteHandler;
    private final ScrollPanel panel;

    public SequenceViewPanel(EntryInfo info) {
        this.info = info;
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        initWidget(layout);

        sequenceDownload = new SequenceFileDownload(info.getId(), info.isHasOriginalSequence());
        sequenceDownload.asWidget().addStyleName("display-inline");

        sequenceUpload = new SequenceFileUpload(info.getId());
        sequenceUpload.asWidget().addStyleName("display-inline");

        layout.setWidget(0, 0, createSequenceHeader());
        layout.getFlexCellFormatter().setColSpan(0, 0, 6);

        layout.setWidget(1, 0, new Label(""));
        layout.getFlexCellFormatter().setHeight(1, 0, "10px");
        layout.getFlexCellFormatter().setColSpan(1, 0, 6);

        // sbol visual
        String imgUrl = "";
        if (info.isHasSequence() && info.getSbolVisualURL() != null) {
            imgUrl = "<img height=\"100px\" src=\"" + info.getSbolVisualURL() + "\" /><br>";
        }

        panel = new ScrollPanel();
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setWidth("100%");
        HTML html = new HTML(imgUrl);

        verticalPanel.add(html);
        panel.add(verticalPanel);
        layout.setWidget(2, 0, panel);
        layout.getFlexCellFormatter().setColSpan(2, 0, 6);

        updateSequenceContents();
        html.setWidth(layout.getOffsetWidth() + "px");
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
            Flash flash = new Flash(param);
            GWT.log(flash.asWidget().getOffsetWidth() + "px");
            layout.setWidget(3, 0, flash);
            layout.getFlexCellFormatter().setHeight(3, 0, "600px");
        } else {
            layout.setHTML(3, 0, "<span class=\"font-80em\"><i>No sequence provided</i></span>");
            layout.getFlexCellFormatter().setHeight(3, 0, "20px");
        }
    }

    private Widget createSequenceHeader() {
        headerPanel = new HTMLPanel(
                "<span style=\"color: #233559; "
                        + "font-weight: bold; font-style: italic; font-size: 0.80em;\">"
                        + "SEQUENCE</span><div style=\"float: right\"><span id=\"delete_sequence_link\"></span>"
                        + "<span id=\"sequence_link\"></span>"
                        + "<span style=\"color: #262626; font-size: 0.75em;\">|</span>"
                        + " <span id=\"sequence_options\"></span>"
                        + " <span style=\"color: #262626; font-size: 0.75em;\">|</span>"
                        + " <span id=\"sbol_visual\"></span></div>");

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
            // sbol visual
            final Label sbVisual = new Label("Hide SBOL Visual");
            sbVisual.setStyleName("open_sequence_sub_link");
            sbVisual.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (layout.getFlexCellFormatter().isVisible(2, 0)) {
                        sbVisual.setText("Show SBOL Visual");
                        layout.getFlexCellFormatter().setVisible(2, 0, false);
                    } else {
                        sbVisual.setText("Hide SBOL Visual");
                        layout.getFlexCellFormatter().setVisible(2, 0, true);
                    }
                }
            });
            headerPanel.add(sbVisual, "sbol_visual");
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
        this.info.setHasOriginalSequence(hasSequence);
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
