package org.jbei.ice.client.entry.view.detail;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.PopupHandler;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

class SequenceFileDownload implements IsWidget {

    private final CellList<DownloadOption> options;
    private final SingleSelectionModel<DownloadOption> optionSelection;
    private final Label label;

    interface Style extends CellList.Style {

        String downloadStyle();
    }

    interface SequenceFileDownloadResource extends CellList.Resources {

        static SequenceFileDownloadResource INSTANCE = GWT
                .create(SequenceFileDownloadResource.class);

        @Source("org/jbei/ice/client/resource/image/arrow_down.png")
        @ImageOptions(repeatStyle = RepeatStyle.None)
        ImageResource sortDown();

        @Source("org/jbei/ice/client/resource/css/SequenceFileDownload.css")
        Style cellListStyle();
    }

    public SequenceFileDownload(final long entryId) {
        SequenceFileDownloadResource.INSTANCE.cellListStyle().ensureInjected();
        label = new Label("Download");
        label.setStyleName(SequenceFileDownloadResource.INSTANCE.cellListStyle().downloadStyle());

        // renderer for options list
        options = new CellList<DownloadOption>(new AbstractCell<DownloadOption>() {

            @Override
            public void render(Context context, DownloadOption value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>" + value.toString() + "</span>");
            }
        }, SequenceFileDownloadResource.INSTANCE);

        options.setRowData(Arrays.asList(DownloadOption.values()));

        final PopupHandler popupHandler = new PopupHandler(options, label.getElement(), 0, 1, false);

        label.addClickHandler(popupHandler);
        optionSelection = new SingleSelectionModel<DownloadOption>();
        optionSelection.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                DownloadOption selected = optionSelection.getSelectedObject();
                if (selected == null)
                    return;
                popupHandler.hidePopup();
                Window.Location.replace("/sequence?type=" + selected.getType() + "&entry="
                        + entryId);
                optionSelection.setSelected(selected, false);
            }
        });

        options.setSelectionModel(optionSelection);
    }

    public SingleSelectionModel<DownloadOption> getSelectionModel() {
        return this.optionSelection;
    }

    @Override
    public Widget asWidget() {
        return label;
    }

    // download options for sequence files
    public enum DownloadOption {
        ORIGINAL("Original", "original"), GENBANK("GenBank", "genbank"), FASTA("FASTA", "fasta");

        private String display;
        private String type;

        private DownloadOption(String display, String type) {
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
