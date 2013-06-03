package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

/**
 * @author Hector Plahar
 */
public class UploadCSV extends Composite {

    private PopupPanel popupPanel;
    private SingleUploader singleUploader;
    private FlexTable table;
    private EntryAddType addType;

    public UploadCSV() {
        String html = "<i class=\"" + FAIconType.CLOUD_UPLOAD.getStyleName() + "\"></i> Upload CSV";
        HTMLPanel panel = new HTMLPanel(html);
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

    public void setType(EntryAddType importType) {
        addType = importType;
        String suffix;
        if (importType == EntryAddType.STRAIN_WITH_PLASMID) {
            suffix = "Strains with one Plasmid";
        } else {
            suffix = importType.getDisplay() + "s";
        }

        table.setHTML(0, 0, "<b>Upload CSV containing " + suffix + "</b>");
    }

    protected void initUploader() {
        singleUploader = new SingleUploader();
        singleUploader.setAutoSubmit(true);
        singleUploader.setStyleName("");

        singleUploader.addOnStartUploadHandler(new IUploader.OnStartUploaderHandler() {
            @Override
            public void onStart(IUploader uploader) {
                String servletPath = "csv_upload?type=" + addType.name() + "&sid=" + ClientController.sessionId;
                uploader.setServletPath(servletPath);
            }
        });

        singleUploader.addOnFinishUploadHandler(new IUploader.OnFinishUploaderHandler() {
            @Override
            public void onFinish(IUploader uploader) {
                if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                    IUploader.UploadedInfo info = uploader.getServerInfo();
                    if (info.message.startsWith("Error")) {
                        // show error message
                        showError(info.message);
                    } else {
                        // TODO : get the bulk upload
                        popupPanel.hide();
                    }
                }

                singleUploader.reset();
            }
        });
    }

    protected Widget getWidget() {
        table = new FlexTable();
        table.setCellPadding(5);
        table.setWidth("600px");
        table.setStyleName("bg_white");

        table.setHTML(0, 0, "<b>Upload CSV</b>");
        HTML close = new HTML("Close <i class=\"" + FAIconType.REMOVE.getStyleName() + "\"></i>");
        close.setStyleName("font-75em");
        table.setWidget(0, 1, close);
        table.getFlexCellFormatter().setWidth(0, 1, "50px");
        table.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        table.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        String html = "<br><div style=\"border-radius: 5px; border: 1px solid #ccc; background-color: #f1f1f1; "
                + "font-size: 0.85em; padding: 3px\">Please read the following carefully before attempting to upload a "
                + "your csv: when an unknown printer took a galley of type and scrambled it to make a type"
                + " specimen book. It has survived not only five centuries</div><br>";
        table.setHTML(1, 0, html);
        table.getFlexCellFormatter().setColSpan(1, 0, 2);

        table.setWidget(2, 0, singleUploader);
        table.getFlexCellFormatter().setColSpan(2, 0, 2);

        close.addStyleName("opacity_hover");
        close.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupPanel.hide();
            }
        });

        return table;
    }

    private class UploadHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            popupPanel.center();
        }
    }

//    public void showSuccess(String password) {
//        String message = "<div style=\"padding: 8px 14px; font-size: 14px; text-shadow: 0 1px 0 #f3f3f3; "
//                + "background-color: #dff0d8; border-color: #468847; border-radius: 4px;\">"
//                + "<i style=\"color: #468847\" class=\"" + FAIconType.OK.getStyleName()
//                + "\"></i> Account with id \"" + userId.getText().trim() + "\" created successfully. "
//                + "The password is shown below <br><br><b style=\"font-size: 16px\">"
//                + password + "</b></div>";
//
//        int row = inputTable.getRowCount() - 1;
//        inputTable.getFlexCellFormatter().setColSpan(row, 0, 2);
//        cancel.setText("Close");
//        inputTable.setHTML(row, 0, message);
//
//        row += 1;
//        inputTable.getFlexCellFormatter().setColSpan(row, 0, 2);
//        cancel.setText("Close");
//        inputTable.setWidget(row, 0, cancel);
//        inputTable.getCellFormatter().setHorizontalAlignment(row, 0, HasAlignment.ALIGN_RIGHT);
//    }

    public void showError(String errorMsg) {
        String message = "<br><div style=\"padding: 8px 14px; font-size: 14px; text-shadow: 0 1px 0 #f3f3f3; "
                + "background-color: #f2dede; border-color: #b94a48; border-radius: 4px;\">"
                + "<span style=\"color: #b94a48\">There was an error processing your file</span><br><br>" + errorMsg
                + "</div>";

        table.getFlexCellFormatter().setColSpan(3, 0, 2);
        table.setHTML(3, 0, message);
    }
}
