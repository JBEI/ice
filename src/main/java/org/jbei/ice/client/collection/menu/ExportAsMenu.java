package org.jbei.ice.client.collection.menu;

import java.util.Arrays;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
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
        exportAs.setStyleName("buttonGroupItem");
        exportAs.addStyleName("firstItem");
        exportAs.addStyleName(ExportAsResource.INSTANCE.cellListStyle().subMenuExport());

        // renderer for options list
        options = new CellList<ExportAsOption>(new AbstractCell<ExportAsOption>() {

            @Override
            public void render(Context context, ExportAsOption value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span style=>" + value.toString() + "</span>");
            }
        }, ExportAsResource.INSTANCE);

        options.setRowData(Arrays.asList(ExportAsOption.values()));

        final MenuClickHandler exportAsClickHandler = new MenuClickHandler(options,
                exportAs.getElement());

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

    //
    // inner classes
    //

    private class MenuClickHandler implements ClickHandler {

        private final PopupPanel popup;

        public MenuClickHandler(Widget widget, Element autoHide) {
            this.popup = new PopupPanel();
            this.popup.setAutoHideEnabled(true);
            this.popup.addAutoHidePartner(autoHide);
            this.popup.setWidget(widget);
            this.popup.setGlassEnabled(true);
        }

        @Override
        public void onClick(ClickEvent event) {
            if (!popup.isShowing()) {
                Widget source = (Widget) event.getSource();
                int x = source.getAbsoluteLeft() - 1;
                int y = source.getOffsetHeight() + source.getAbsoluteTop() + 1;
                popup.setPopupPosition(x, y);
                popup.show();
            } else {
                popup.hide();
            }
        }

        public void hidePopup() {
            if (this.popup == null || !this.popup.isShowing())
                return;

            this.popup.hide();
        }
    }
}
