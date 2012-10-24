package org.jbei.ice.client.collection.menu;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PopupHandler;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class ExportAsMenu implements IsWidget {

    interface Style extends CellList.Style {

        String subMenuExport();
    }

    interface ExportAsResource extends CellList.Resources {

        static ExportAsResource INSTANCE = GWT.create(ExportAsResource.class);

        @Source("org/jbei/ice/client/resource/css/ExportAs.css")
        Style cellListStyle();
    }

    private static final String LABEL = "<i class=\"" + FAIconType.DOWNLOAD_ALT.getStyleName()
            + "\" style=\"opacity:0.85; color: #0082C0\"></i> "
            + "Export As <i class=\"" + FAIconType.CARET_DOWN.getStyleName() + "\"></i>";
    private final Button exportAs;
    private final SingleSelectionModel<ExportAsOption> optionSelection;

    public ExportAsMenu() {
        ExportAsResource.INSTANCE.cellListStyle().ensureInjected();
        exportAs = new Button(LABEL);
        exportAs.setStyleName(ExportAsResource.INSTANCE.cellListStyle().subMenuExport());

        // renderer for options list
        CellList<ExportAsOption> options = new CellList<ExportAsOption>(new AbstractCell<ExportAsOption>() {

            @Override
            public void render(Context context, ExportAsOption value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>" + value.toString() + "</span>");
            }
        }, ExportAsResource.INSTANCE);

        options.setRowData(Arrays.asList(ExportAsOption.values()));

        final PopupHandler exportAsClickHandler = new PopupHandler(options, exportAs.getElement(),
                                                                   0, 0, false);

        exportAs.addClickHandler(exportAsClickHandler);
        optionSelection = new SingleSelectionModel<ExportAsOption>();
        optionSelection.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                exportAsClickHandler.hidePopup();
            }
        });

        options.setSelectionModel(optionSelection);
        exportAs.setEnabled(false);
    }

    public SingleSelectionModel<ExportAsOption> getSelectionModel() {
        return this.optionSelection;
    }

    public void enable(boolean enable) {
        this.exportAs.setEnabled(enable);
    }

    @Override
    public Widget asWidget() {
        return exportAs;
    }
}
