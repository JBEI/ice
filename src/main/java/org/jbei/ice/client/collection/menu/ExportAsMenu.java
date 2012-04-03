package org.jbei.ice.client.collection.menu;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.PopupHandler;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
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

        @Source("org/jbei/ice/client/resource/image/arrow_down.png")
        @ImageOptions(repeatStyle = RepeatStyle.None)
        ImageResource sortDown();

        @Source("org/jbei/ice/client/resource/css/ExportAs.css")
        Style cellListStyle();
    }

    private static final String LABEL = "Export As";
    private final Button exportAs;
    private final CellList<ExportAsOption> options;
    private final SingleSelectionModel<ExportAsOption> optionSelection;

    public ExportAsMenu() {
        ExportAsResource.INSTANCE.cellListStyle().ensureInjected();
        exportAs = new Button(LABEL);
        exportAs.setStyleName(ExportAsResource.INSTANCE.cellListStyle().subMenuExport());

        // renderer for options list
        options = new CellList<ExportAsOption>(new AbstractCell<ExportAsOption>() {

            @Override
            public void render(Context context, ExportAsOption value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>" + value.toString() + "</span>");
            }
        }, ExportAsResource.INSTANCE);

        options.setRowData(Arrays.asList(ExportAsOption.values()));

        final PopupHandler exportAsClickHandler = new PopupHandler(options, exportAs.getElement(),
                0, 0);

        exportAs.addClickHandler(exportAsClickHandler);
        optionSelection = new SingleSelectionModel<ExportAsOption>();
        optionSelection.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                exportAsClickHandler.hidePopup();
            }
        });

        options.setSelectionModel(optionSelection);
    }

    public SingleSelectionModel<ExportAsOption> getSelectionModel() {
        return this.optionSelection;
    }

    @Override
    public Widget asWidget() {
        return exportAs;
    }
}
