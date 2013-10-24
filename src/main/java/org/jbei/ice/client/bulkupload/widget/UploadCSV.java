package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.GenericPopup;
import org.jbei.ice.client.common.widget.ICanReset;
import org.jbei.ice.lib.shared.EntryAddType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget to enable upload of csv files. Places a "CSV Upload" label
 * which when clicked, shows a popup that the user can interact with to upload the csv file
 *
 * @author Hector Plahar
 */
public class UploadCSV extends Composite {

    private HTML label;
    private HTML templateDownloadLabel;
    private HandlerRegistration handler;
    private GenericPopup dialog;
    private EntryAddType addType;
    private ServiceDelegate<Long> delegate;

    public UploadCSV() {
        label = new HTML("<i class=\"" + FAIconType.TABLE.getStyleName() + "\"></i> CSV Upload");
        label.setStyleName("bulk_upload_visibility");
        label.addStyleName("opacity_hover");
        initWidget(label);

        templateDownloadLabel = new HTML("Click Here");

        initFileUpload();
        addHandlers();
    }

    public void setAddType(EntryAddType addType) {
        this.addType = addType;
    }

    protected void addHandlers() {
        if (handler != null)
            handler.removeHandler();
        handler = label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.showDialog();
            }
        });

        templateDownloadLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (addType == null)
                    return;

                Window.Location.replace("/download?type=template&add_type=" + addType.name());
            }
        });
    }

    protected void initFileUpload() {
        final FormPanel formPanel = new FormPanel();
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);

        final FileUpload fileUpload = new FileUpload();
        fileUpload.setName("uploadFormElement");
        formPanel.add(fileUpload);

        final PopupLayout layout = new PopupLayout(formPanel, templateDownloadLabel);

        dialog = new GenericPopup(layout, "<b>CSV Upload</b>");

        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String msg = event.getResults();
                boolean success = !msg.startsWith("Error");
                if (success) {
                    try {
                        delegate.execute(Long.decode(msg.trim()));
                        dialog.hideDialog();
                    } catch (NumberFormatException nfe) {
                        layout.setErrorMessage("There was an unknown problem creating the parts from the file");
                    }
                } else
                    layout.setErrorMessage(msg);

                formPanel.reset();
            }
        });

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fileUpload.getFilename().isEmpty())
                    return;

                formPanel.setAction(
                        "/upload?sid=" + ClientController.sessionId + "&type=bulk_csv&upload=" + addType.name());
                formPanel.submit();
            }
        });
    }

    public void setDelegate(ServiceDelegate<Long> handler) {
        delegate = handler;
    }

    /**
     * Layout for the popup shown when user clicks "CSV Upload"
     */
    private class PopupLayout implements ICanReset {

        private FlexTable table;
        private final FormPanel panel;

        public PopupLayout(FormPanel formPanel, Label label) {
            this.panel = formPanel;
            this.table = new FlexTable();
            String html = "<p style=\"padding: 6px; border: 1px solid #eee; background-color: #f1f1f1; color: #555;"
                    + "font-size: 0.80em; border-radius: 3px; \"><i style=\"font-size:70px; float: left;"
                    + "padding-right:10px; color: #3a87ad\" class=\"" + FAIconType.INFO_CIRCLE.getStyleName()
                    + "\"></i>The first line of your CSV file should contain"
                    + " the header information and must be spelled exactly as it appears in the web interface. e.g. "
                    + "<b>\"Principal Investigator\", \"Funding Source\", \"Intellectual Property\",</b> etc.<br><br>"
                    + "<span id=\"template_download\"></span> to download a template for the CSV upload.</p>";
            HTMLPanel panel = new HTMLPanel(html);
            panel.add(label, "template_download");
            label.setStyleName("footer_feedback_widget");
            label.addStyleName("display-inline");

            table.setWidget(0, 0, panel);
            table.setWidget(1, 0, formPanel);
            table.setHTML(2, 0, "");
            table.getFlexCellFormatter().setVisible(2, 0, false);
            table.getFlexCellFormatter().setStyleName(2, 0, "login_error_msg");
        }

        public void setErrorMessage(String message) {
            table.setHTML(2, 0, "<p>" + message + "</p>");
            table.getFlexCellFormatter().setVisible(2, 0, true);
        }

        @Override
        public void reset() {
            table.getFlexCellFormatter().setVisible(2, 0, false);
            table.setHTML(2, 0, "");
            panel.reset();
        }

        @Override
        public Widget asWidget() {
            return table;
        }
    }
}
