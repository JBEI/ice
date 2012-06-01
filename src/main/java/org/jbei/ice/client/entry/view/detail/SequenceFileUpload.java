package org.jbei.ice.client.entry.view.detail;

import gwtupload.client.IUploader.OnFinishUploaderHandler;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.client.entry.view.detail.SequenceFileDownload.SequenceFileDownloadResource;
import org.jbei.ice.client.entry.view.detail.SequenceFileUploadPresenter.IView;
import org.jbei.ice.client.entry.view.detail.SequenceFileUploadPresenter.UploadOption;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * UI widget that allows users to upload sequence information
 * and associate them with a specified entry (via constructor param).
 * 
 * @author Hector Plahar
 */
class SequenceFileUpload implements IsWidget, IView {

    interface Style extends CellList.Style {

        String downloadStyle();
    }

    interface SequenceFileUploadResource extends CellList.Resources {

        static SequenceFileUploadResource INSTANCE = GWT.create(SequenceFileUploadResource.class);

        @Source("org/jbei/ice/client/resource/image/arrow_down.png")
        @ImageOptions(repeatStyle = RepeatStyle.None)
        ImageResource sortDown();

        @Source("org/jbei/ice/client/resource/css/SequenceFileUpload.css")
        Style cellListStyle();
    }

    private final CellList<UploadOption> options;
    private final SingleSelectionModel<UploadOption> optionSelection;
    private final Label label;
    private final PasteSequenceWidget pasteSequenceWidget;
    private final UploadSequenceFileWidget fileUploadWidget;
    private final PopupHandler popupHandler;
    private final SequenceFileUploadPresenter presenter;

    public SequenceFileUpload(final long entryId) {
        SequenceFileUploadResource.INSTANCE.cellListStyle().ensureInjected();
        label = new Label("Upload");
        label.setStyleName(SequenceFileUploadResource.INSTANCE.cellListStyle().downloadStyle());

        // renderer for options list
        options = new CellList<UploadOption>(new AbstractCell<UploadOption>() {

            @Override
            public void render(Context context, UploadOption value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>" + value.toString() + "</span>");
            }
        }, SequenceFileDownloadResource.INSTANCE);

        options.setRowData(Arrays.asList(UploadOption.values()));

        popupHandler = new PopupHandler(options, label.getElement(), 0, 1, false);
        label.addClickHandler(popupHandler);

        optionSelection = new SingleSelectionModel<UploadOption>();
        options.setSelectionModel(optionSelection);
        pasteSequenceWidget = new PasteSequenceWidget();
        fileUploadWidget = new UploadSequenceFileWidget(entryId);
        presenter = new SequenceFileUploadPresenter(this);
    }

    public void setFileUploadWidgetFinishHandler(OnFinishUploaderHandler handler) {
        this.fileUploadWidget.setFinishHandler(handler);
    }

    public SequenceFileUploadPresenter getPresenter() {
        return this.presenter;
    }

    @Override
    public void addSubmitSequencePasteHandler(ClickHandler handler) {
        pasteSequenceWidget.addSaveHandler(handler);
    }

    @Override
    public SingleSelectionModel<UploadOption> getUploadOptionSelectionModel() {
        return this.optionSelection;
    }

    @Override
    public String getPastedSequence() {
        return pasteSequenceWidget.getSequence();
    }

    @Override
    public Widget asWidget() {
        return label;
    }

    @Override
    public void showPasteSequenceDialog() {
        pasteSequenceWidget.showDialog();
        popupHandler.hidePopup();
    }

    @Override
    public void showUploadFileDialog() {
        fileUploadWidget.showDialog();
        popupHandler.hidePopup();
    }

    public void hidePasteDialog() {
        pasteSequenceWidget.hideDialog();
    }
}
