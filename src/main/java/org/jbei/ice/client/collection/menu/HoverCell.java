package org.jbei.ice.client.collection.menu;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Menu cell when user hovers over a menu item
 *
 * @author Hector Plahar
 */

public class HoverCell extends Composite {
    private final HTMLPanel panel;
    private final Icon icon;
    private final CellList<HoverOptions> options;
    private final PopupHandler popupHandler;
    private final SingleSelectionModel<HoverOptions> optionSelection;

    interface HoverCellListResource extends CellList.Resources {

        static HoverCellListResource INSTANCE = GWT.create(HoverCellListResource.class);

        @Source("org/jbei/ice/client/resource/css/HoverCellListResource.css")
        CellList.Style cellListStyle();
    }

    public HoverCell() {
        panel = new HTMLPanel("<span id=\"icon\"></span>");
        icon = new Icon(FAIconType.CARET_DOWN);
        initWidget(panel);

        HoverCellListResource.INSTANCE.cellListStyle().ensureInjected();
        panel.add(icon, "icon");
        panel.setStyleName("collection_options");
        options = new CellList<HoverOptions>(new AbstractCell<HoverOptions>() {
            @Override
            public void render(Context context, HoverOptions value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant(
                        "<i style=\"display: inline-block; width: 1.3em; text-align: left\" class=\""
                                + value.getIcon().getStyleName()
                                + "\"></i><span>" + value.toString() + "</span>");
            }
        }, HoverCellListResource.INSTANCE);

        options.setRowData(Arrays.asList(HoverOptions.values()));

        popupHandler = new PopupHandler(options, panel.getElement(), 0, 0, false);
        panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupHandler.onClick(event);
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        optionSelection = new SingleSelectionModel<HoverOptions>();
        options.setSelectionModel(optionSelection);
    }

    public SingleSelectionModel<HoverOptions> getOptionSelection() {
        return optionSelection;
    }

    public boolean optionsAreVisible() {
        return popupHandler.popupIsShowing();
    }

    public void hideOptions() {
        popupHandler.hidePopup();
    }

    public void addOptionsCloseHandler(CloseHandler<PopupPanel> handler) {
        popupHandler.setCloseHandler(handler);
    }

    public enum HoverOptions {

        EDIT(FAIconType.EDIT, "Rename"),
        DELETE(FAIconType.TRASH, "Delete"),
        SHARE(FAIconType.GROUP, "Share");

        private final String display;
        private final FAIconType icon;

        HoverOptions(FAIconType icon, String display) {
            this.display = display;
            this.icon = icon;
        }

        public String toString() {
            return this.display;
        }

        public FAIconType getIcon() {
            return this.icon;
        }
    }
}
