package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Collection Selection menu
 * 
 * @author Hector Plahar
 */
public class SelectionMenu extends Composite {

    private final HorizontalPanel menuHolder;
    private UserCollectionMultiSelect addToSelection;
    private UserCollectionMultiSelect moveToToSelection;
    private Button addToSubmit;
    private Button moveToSubmit;
    private final ListDataProvider<FolderDetails> dataProvider;

    public SelectionMenu(ListDataProvider<FolderDetails> dataProvider) {

        menuHolder = new HorizontalPanel();
        initWidget(menuHolder);

        this.dataProvider = dataProvider;

        createAddToMenu();
        createRemoveMenu();
        createMoveMenu();
        createExportMenu();
    }

    protected void createAddToMenu() {
        final Button addTo = new Button("Add To");
        addToSubmit = new Button("Submit");
        addToSelection = new UserCollectionMultiSelect(addToSubmit, this.dataProvider);
        MenuClickHandler addToHandler = new MenuClickHandler(addToSelection, addTo.getElement());
        addTo.addClickHandler(addToHandler);
        menuHolder.add(addTo);
    }

    protected void createRemoveMenu() {
        Button remove = new Button("Remove");
        menuHolder.add(remove);
    }

    protected void createMoveMenu() {
        final Button move = new Button("Move To");
        moveToSubmit = new Button("Submit");
        moveToToSelection = new UserCollectionMultiSelect(moveToSubmit, this.dataProvider);
        MenuClickHandler clickHandler = new MenuClickHandler(moveToToSelection, move.getElement());
        move.addClickHandler(clickHandler);
        menuHolder.add(move);
        menuHolder.add(new HTML("&nbsp;"));
    }

    protected void createExportMenu() {
        Button exportAs = new Button("Export As");
        CellList<ExportOption> options = new CellList<ExportOption>(
                new AbstractCell<ExportOption>() {

                    @Override
                    public void render(Context context, ExportOption value, SafeHtmlBuilder sb) {
                        sb.appendHtmlConstant("<span>" + value.getDisplay() + "</span>");
                    }
                });

        ArrayList<ExportOption> list = new ArrayList<ExportOption>();
        list.add(ExportOption.PRINTABLE);
        list.add(ExportOption.EXCEL);
        list.add(ExportOption.XML);
        options.setRowData(list);

        final MenuClickHandler exportAsClickHandler = new MenuClickHandler(options,
                exportAs.getElement());

        exportAs.addClickHandler(exportAsClickHandler);

        final SingleSelectionModel<ExportOption> optionSelection = new SingleSelectionModel<ExportOption>();

        optionSelection.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                ExportOption selected = optionSelection.getSelectedObject();
                Window.alert(selected.getDisplay());
                exportAsClickHandler.hidePopup();
            }
        });

        options.setSelectionModel(optionSelection);
        menuHolder.add(exportAs);
    }

    public Button getAddToSubmit() {
        return this.addToSubmit;
    }

    public Button getMoveToSubmit() {
        return this.moveToSubmit;
    }

    public Set<FolderDetails> getMoveToDestination() {
        return moveToToSelection.getSelected();
    }

    public Set<FolderDetails> getAddToDestination() {
        return this.addToSelection.getSelected();
    }

    //
    // inner classes
    //

    private enum ExportOption {

        PRINTABLE("Printable"), EXCEL("String"), XML("XML");

        private String display;

        ExportOption(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return this.display;
        }
    }

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
                int x = source.getAbsoluteLeft();
                int y = source.getOffsetHeight() + source.getAbsoluteTop();
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
