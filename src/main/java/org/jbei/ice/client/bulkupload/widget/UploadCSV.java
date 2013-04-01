package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

/**
 * @author Hector Plahar
 */
public class UploadCSV extends Composite {

    private final HTMLPanel panel;
    private PopupPanel popupPanel;
    private SingleUploader uploader;

    public UploadCSV() {
        String html = "<i class=\"" + FAIconType.CLOUD_UPLOAD.getStyleName() + "\"></i> Upload CSV";
        panel = new HTMLPanel(html);
        panel.setStyleName("bulk_upload_visibility");
        panel.addStyleName("opacity_hover");
        initWidget(panel);
        initUploader();


        panel.addDomHandler(new UploadHandler(), ClickEvent.getType());
        createPopupPanel();

    }

    public void createPopupPanel() {
        popupPanel = new PopupPanel();
        popupPanel.setGlassEnabled(true);
        popupPanel.setWidget(getWidget());
        popupPanel.setGlassStyleName("popup_panel_glass");
    }

    protected void initUploader() {
        uploader = new SingleUploader();
        uploader.setAutoSubmit(true);

        uploader.addOnStartUploadHandler(new IUploader.OnStartUploaderHandler() {
            @Override
            public void onStart(IUploader uploader) {
                String servletPath = "csv_upload?eid=&type=sequence&sid=" + ClientController.sessionId;
                uploader.setServletPath(servletPath);
            }
        });
    }

    protected Widget getWidget() {
        FlexTable table = new FlexTable();
        table.setWidth("500px");
        table.setStyleName("bg_white");

        HTMLPanel htmlPanel = new HTMLPanel("<h4>Upload CSV</h4><span style=\"float: right\" id=\"x-close\"></span>");
        HTML close = new HTML("<i class=\"" + FAIconType.REMOVE.getStyleName() + "\"></i>");
        htmlPanel.add(close, "x-close");
        table.setWidget(0, 0, htmlPanel);

        String html = "<span style=\"padding: 3px\">ndustry. Lorem Ipsum has been the industry's standard dummy text " +
                "ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type" +
                " specimen book. It has survived not only five centuries, but also the leap into electronic" +
                " typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release" +
                " of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing" +
                " software like Aldus PageMaker including versions of Lorem Ipsum.</span>";
        table.setHTML(1, 0, html);
        table.setWidget(2, 0, uploader);

        return table;
    }

    private class UploadHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            popupPanel.center();
        }
    }
}
