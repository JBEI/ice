package org.jbei.ice.client.entry.display.detail;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.client.entry.display.detail.SequenceFileDownload.SequenceFileDownloadResource;
import org.jbei.ice.client.entry.display.detail.SequenceFileUploadPresenter.IView;

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
 * UI widget that allows users to upload or paste sequence information
 * and associate them with a specified entry or have a new one created if being used
 * in a create entry form
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

    private final SingleSelectionModel<UploadOption> optionSelection;
    private final Label label;
    private final PasteSequenceWidget pasteSequenceWidget;
    private final UploadSequenceFileWidget fileUploadWidget;
    private final PopupHandler popupHandler;
    private boolean pasteAction;

    public SequenceFileUpload() {
        SequenceFileUploadResource.INSTANCE.cellListStyle().ensureInjected();
        label = new Label("Add Sequence");
        label.setStyleName(SequenceFileUploadResource.INSTANCE.cellListStyle().downloadStyle());

        // renderer for options list
        CellList<UploadOption> options = new CellList<UploadOption>(new AbstractCell<UploadOption>() {

            @Override
            public void render(Context context, UploadOption value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span class=\"font-85em\"><i class=\"" + value.getIconType().getStyleName()
                                              + "\"></i> " + value.toString() + "</span>");
            }
        }, SequenceFileDownloadResource.INSTANCE);

        options.setRowData(Arrays.asList(UploadOption.values()));

        popupHandler = new PopupHandler(options, label.getElement(), false);
        label.addClickHandler(popupHandler);

        optionSelection = new SingleSelectionModel<UploadOption>();
        options.setSelectionModel(optionSelection);
        pasteSequenceWidget = new PasteSequenceWidget();
        fileUploadWidget = new UploadSequenceFileWidget();
        new SequenceFileUploadPresenter(this);
    }

    public void addSubmitSequencePasteHandler(ClickHandler handler) {
        pasteSequenceWidget.addSaveHandler(handler);
        fileUploadWidget.addSaveHandler(handler);
    }

    @Override
    public SingleSelectionModel<UploadOption> getUploadOptionSelectionModel() {
        return this.optionSelection;
    }

    @Override
    public String getPastedSequence() {
        if (pasteAction)
            return pasteSequenceWidget.getSequence();
        return fileUploadWidget.getFileName();
    }

    public boolean isPasteAction() {
        return this.pasteAction;
    }

    @Override
    public Widget asWidget() {
        return label;
    }

    @Override
    public void showPasteSequenceDialog() {
        pasteAction = true;
        pasteSequenceWidget.showDialog();
        popupHandler.hidePopup();
    }

    @Override
    public void showUploadFileDialog() {
        pasteAction = false;
        fileUploadWidget.showDialog();
        popupHandler.hidePopup();
    }

    public void hidePasteDialog() {
        if (pasteAction)
            pasteSequenceWidget.hideDialog();
        else
            fileUploadWidget.hideDialog();
    }
}
