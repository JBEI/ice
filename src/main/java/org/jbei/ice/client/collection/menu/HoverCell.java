package org.jbei.ice.client.collection.menu;

import java.util.List;

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

    private final PopupHandler popupHandler;
    private final SingleSelectionModel<HoverOption> optionSelection;
    private final CellList<HoverOption> options;

    interface HoverCellListResource extends CellList.Resources {

        static HoverCellListResource INSTANCE = GWT.create(HoverCellListResource.class);

        @Source("org/jbei/ice/client/resource/css/HoverCellListResource.css")
        CellList.Style cellListStyle();
    }

    public HoverCell() {
        HTMLPanel panel = new HTMLPanel("<span id=\"icon\"></span>");
        Icon icon = new Icon(FAIconType.CARET_DOWN);
        initWidget(panel);

        HoverCellListResource.INSTANCE.cellListStyle().ensureInjected();
        panel.add(icon, "icon");
        panel.setStyleName("collection_options");
        options = new CellList<HoverOption>(new AbstractCell<HoverOption>() {
            @Override
            public void render(Context context, HoverOption value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant(
                        "<i style=\"display: inline-block; width: 1.3em; text-align: left\" class=\""
                                + value.getIcon().getStyleName()
                                + "\"></i><span>" + value.toString() + "</span>");
            }
        }, HoverCellListResource.INSTANCE);
        options.setWidth("80px");

        popupHandler = new PopupHandler(options, panel.getElement(), false);
        panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupHandler.onClick(event);
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        optionSelection = new SingleSelectionModel<HoverOption>();
        options.setSelectionModel(optionSelection);
    }

    public void setHoverOptions(List<HoverOption> optionList) {
        options.setRowData(optionList);
    }

    public SingleSelectionModel<HoverOption> getOptionSelection() {
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
}
