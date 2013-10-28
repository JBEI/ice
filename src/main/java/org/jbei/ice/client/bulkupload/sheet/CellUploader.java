package org.jbei.ice.client.bulkupload.sheet;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.entry.EntryType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget used to upload files in the bulk upload interface
 *
 * @author Hector Plahar
 */
public class CellUploader extends Composite {

    private HTML fileUploadImg;
    private HorizontalPanel panel;
    private long currentId;
    private final FormPanel formPanel;
    private FileUpload fileUpload;
    private int currentRow;
    private boolean sequenceUpload;
    private HandlerRegistration registration;

    public CellUploader(final EntryInfoDelegate delegate, final EntryAddType addType, final EntryType type) {
        fileUploadImg = new HTML("<i class=\"" + FAIconType.UPLOAD.getStyleName() + "\"></i>");
        fileUploadImg.addStyleName("cursor_pointer");
        fileUploadImg.addStyleName("opacity_hover");
        fileUploadImg.addStyleName("font-75em");

        panel = new HorizontalPanel();
        panel.setWidth("100%");
        initWidget(panel);

        panel.add(fileUploadImg);
        formPanel = fileUploadPanel(delegate, type.name(), addType.name());
        panel.add(formPanel);

        fileUploadImg.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fileClick(fileUpload.getElement());
            }
        });
    }

    native void fileClick(Element element) /*-{
        element.click();
    }-*/;

    protected FormPanel fileUploadPanel(final EntryInfoDelegate delegate, final String typeName,
            final String addTypeName) {
        final FormPanel formPanel = new FormPanel();
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);

        fileUpload = new FileUpload();
        fileUpload.setName("uploadFormElement");
        fileUpload.setVisible(false);
        formPanel.add(fileUpload);

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fileUpload.getFilename() == null || fileUpload.getFilename().isEmpty())
                    return;

                formPanel.submit();
            }
        });

        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                currentId = delegate.getEntryIdForRow(currentRow);
                long bid = delegate.getBulkUploadId();
                formPanel.setAction("/upload?type=bulk_file_upload&is_sequence="
                                            + Boolean.toString(sequenceUpload)
                                            + "&sid=" + ClientController.sessionId + "&eid=" + currentId
                                            + "&bid=" + bid + "&entry_type=" + typeName
                                            + "&entry_add_type=" + addTypeName);
            }
        });

        return formPanel;
    }

    public long getCurrentId() {
        return this.currentId;
    }

    public void submitClick() {
        fileClick(fileUploadImg.getElement());
    }

    public void addOnFinishUploadHandler(FormPanel.SubmitCompleteHandler handler) {
        if (registration != null)
            registration.removeHandler();

        registration = formPanel.addSubmitCompleteHandler(handler);
    }

    public void setPanelWidget(final Widget widget) {
        panel.clear();
        panel.add(fileUploadImg);
        panel.add(formPanel);
        panel.add(widget);
    }

    public void resetPanelWidget() {
        panel.clear();
        panel.add(fileUploadImg);
        panel.add(formPanel);
    }

    public void resetForm() {
        formPanel.reset();
    }

    public HorizontalPanel getPanel() {
        return this.panel;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    public int getCurrentRow() {
        return this.currentRow;
    }

    public void setSequenceUpload(boolean sequenceUpload) {
        this.sequenceUpload = sequenceUpload;
    }
}
