package org.jbei.ice.client.entry.display.detail;

import java.util.NoSuchElementException;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.FAIconType;
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

/**
 * Widget that displays the sequence file on the general view
 *
 * @author Hector Plahar
 */
public class SequenceViewPanel extends Composite implements ISequenceView {

    private final SequenceFileDownload sequenceDownload;
    private final SequenceFileUpload sequenceUpload;
    private PartData partData;
    private final FlexTable layout;
    private HTMLPanel headerPanel;
    private final SequenceViewPanelPresenter presenter;
    private DeleteSequenceHandler deleteHandler;
    private String header;

    public SequenceViewPanel(PartData partData, String header) {
        this.header = header;
        this.partData = partData;
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        initWidget(layout);

        sequenceDownload = new SequenceFileDownload(partData.getId(), partData.isHasOriginalSequence());
        sequenceDownload.asWidget().addStyleName("display-inline");

        sequenceUpload = new SequenceFileUpload();
        sequenceUpload.asWidget().addStyleName("display-inline");

        createSequenceHeader();

        layout.setWidget(1, 0, new Label(""));
        layout.getFlexCellFormatter().setHeight(1, 0, "10px");
        layout.getFlexCellFormatter().setColSpan(1, 0, 6);

        // SBOL visual
        String imgUrl = "";
        if (partData.isHasSequence() && partData.getSbolVisualURL() != null) {
            imgUrl = "<img height=\"170px\" src=\"/download?type=sbol_visual&id=" + partData.getSbolVisualURL()
                    + "\" /><br>";
        }

        ScrollPanel panel = new ScrollPanel();
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setWidth("100%");
        HTML imageHtml = new HTML(imgUrl);

        verticalPanel.add(imageHtml);
        panel.add(verticalPanel);
        layout.setWidget(2, 0, panel);
        layout.getFlexCellFormatter().setColSpan(2, 0, 6);

        updateSequenceContents();
        imageHtml.setWidth(layout.getOffsetWidth() + "px");

        // new header with only the update option
        String html = "<span style=\"color: #233559; "
                + "font-weight: bold; font-style: italic; font-size: 0.80em;\">" + header.toUpperCase() + "</span>"
                + "<div style=\"float: right\">"
                + "<span id=\"sequence_options\"></span></div>";

        HTMLPanel newHeader = new HTMLPanel(html);
        newHeader.setStyleName("entry_sequence_sub_header");
        newHeader.add(sequenceUpload.asWidget(), "sequence_options");
        layout.setWidget(0, 0, newHeader);

        this.presenter = new SequenceViewPanelPresenter(this);
    }

    /**
     * switches panel to edit mode by hiding elements that are not
     * necessarily useful like pigeon image view
     */
    public void switchToEditMode() {
        // hide pigeon image
        layout.getFlexCellFormatter().setVisible(2, 0, false);

        // new header with only the update option
        String html = "<span style=\"color: #233559; "
                + "font-weight: bold; font-style: italic; font-size: 0.80em;\">" + header.toUpperCase() + "</span>"
                + "<div style=\"float: right\">"
                + "<span id=\"sequence_options\"></span></div>";

        HTMLPanel newHeader = new HTMLPanel(html);
        newHeader.setStyleName("entry_sequence_sub_header");
        newHeader.add(sequenceUpload.asWidget(), "sequence_options");
        layout.setWidget(0, 0, newHeader);
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

    @Override
    public boolean isPastedSequence() {
        return sequenceUpload.isPasteAction();
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
            layout.getFlexCellFormatter().setVisible(2, 0, true);
        } else {
            layout.getFlexCellFormatter().setVisible(2, 0, false);
            String html = "<span class=\"font-80em\"><i style=\"color: #999\">No sequence data provided</i></span>";
            layout.setHTML(3, 0, html);
            layout.getFlexCellFormatter().setHeight(3, 0, "20px");
        }
    }

    private void createSequenceHeader() {
        String html = "<span style=\"color: #233559; "
                + "font-weight: bold; font-style: italic; font-size: 0.80em;\">" + header.toUpperCase() + "</span>"
                + "<div style=\"float: right\"><span id=\"delete_sequence_link\"></span>"
                + "<span id=\"sequence_link\"></span>"
                + "<span id=\"header_separator_pipe\"></span>"
                + "<span id=\"sequence_options\"></span>";

        if (partData.isHasSequence()) {
            html += " <span style=\"color: #262626; font-size: 0.75em;\">|</span> <span id=\"sbol_visual\"></span>";
        }
        html += "</div>";

        headerPanel = new HTMLPanel(html);
        headerPanel.setStyleName("entry_sequence_sub_header");
        updateSequenceHeaders();
        layout.getFlexCellFormatter().setColSpan(0, 0, 6);
    }

    @Override
    public void updateSequenceHeaders() {
        headerPanel.clear();

        if (partData.isHasSequence()) {
            // delete, open in vector editor, download
            HTML openSequenceLabel = new HTML("Open in VectorEditor <i class=\""
                                                      + FAIconType.EXTERNAL_LINK.getStyleName() + "\"></i>");
            openSequenceLabel.addClickHandler(new SequenceHeaderHandler());
            openSequenceLabel.setStyleName("open_sequence_sub_link");
            HTML pipe = new HTML("<span style=\"color: #262626; font-size: 0.75em;\">|</span>");
            pipe.setStyleName("display-inline");

            headerPanel.add(openSequenceLabel, "sequence_link");
            headerPanel.add(pipe, "header_separator_pipe");
            headerPanel.add(sequenceDownload.asWidget(), "sequence_options");
            if (deleteHandler != null)
                showSequenceDeleteLink(deleteHandler);

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
            try {
                headerPanel.add(sbVisual, "sbol_visual");
            } catch (NoSuchElementException noSuchElementException) {
                // edit mode
            }
        } else {
            if (partData.isCanEdit()) {
                // Create New | upload
                HTML label = new HTML("Create in VectorEditor <i class=\"" + FAIconType.EXTERNAL_LINK.getStyleName()
                                              + "\"></i>");
                label.addClickHandler(new SequenceHeaderHandler());
                label.setStyleName("open_sequence_sub_link");
                HTML pipe = new HTML("<span style=\"color: #262626; font-size: 0.75em;\">|</span>");
                pipe.setStyleName("display-inline");
                headerPanel.add(pipe, "header_separator_pipe");
                headerPanel.add(label, "sequence_link");
                headerPanel.add(sequenceUpload.asWidget(), "sequence_options");
            }
        }

        layout.setWidget(0, 0, headerPanel);
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
