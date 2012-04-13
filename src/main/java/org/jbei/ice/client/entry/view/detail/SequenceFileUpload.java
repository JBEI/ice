package org.jbei.ice.client.entry.view.detail;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.client.entry.view.detail.SequenceFileDownload.SequenceFileDownloadResource;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

class SequenceFileUpload implements IsWidget {

    private final CellList<UploadOption> options;
    private final SingleSelectionModel<UploadOption> optionSelection;
    private final Label label;
    private final PasteSequenceWidget sequenceUploadWidget;

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

        final PopupHandler popupHandler = new PopupHandler(options, label.getElement(), 0, 1);

        label.addClickHandler(popupHandler);
        optionSelection = new SingleSelectionModel<UploadOption>();
        optionSelection.addSelectionChangeHandler(new Handler() {

            // TODO : move logic
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                UploadOption selected = optionSelection.getSelectedObject();
                if (selected == null)
                    return;

                sequenceUploadWidget.showDialog();
                popupHandler.hidePopup();
                optionSelection.setSelected(selected, false);
            }
        });

        options.setSelectionModel(optionSelection);
        sequenceUploadWidget = new PasteSequenceWidget();
    }

    public SingleSelectionModel<UploadOption> getSelectionModel() {
        return this.optionSelection;
    }

    @Override
    public Widget asWidget() {
        return label;
    }

    // download options for sequence files
    public enum UploadOption {
        FILE("File Upload", "file"), PASTE("Paste Sequence", "paste");

        private String display;
        private String type;

        private UploadOption(String display, String type) {
            this.display = display;
            this.type = type;
        }

        public String toString() {
            return this.display;
        }

        public String getType() {
            return this.type;
        }
    }
}
