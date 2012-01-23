package org.jbei.ice.client.collection.menu;

import java.util.Set;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sub-menu for manipulating collection of entries.
 * Actions are "Add To", "Remove" and "Move To"
 * 
 * @author Hector Plahar
 * 
 */
public class CollectionSubMenu implements IsWidget {
    private final FlexTable menuHolder;

    private final UserCollectionMultiSelect addToSelection;
    private final UserCollectionMultiSelect moveToSelection;
    private final Button addToButton;
    private final Button removeButton;
    private final Button moveToButton;

    public CollectionSubMenu(UserCollectionMultiSelect addToSelection,
            UserCollectionMultiSelect moveToSelection) {
        this.menuHolder = new FlexTable();
        this.menuHolder.setCellPadding(0);
        this.menuHolder.setCellSpacing(0);

        this.addToSelection = addToSelection;
        this.moveToSelection = moveToSelection;

        addToButton = createAddToButton();
        this.menuHolder.setWidget(0, 0, addToButton);

        removeButton = createRemoveMenu();
        this.menuHolder.setWidget(0, 1, removeButton);

        moveToButton = createMoveMenu();
        this.menuHolder.setWidget(0, 2, moveToButton);
    }

    private Button createAddToButton() {
        final Button addTo = new Button("Add To");
        addTo.setStyleName("buttonGroupItem");
        addTo.addStyleName("firstItem");
        addTo.addStyleName("dropDownAdd");
        MenuClickHandler addToHandler = new MenuClickHandler(addToSelection, addTo.getElement());
        addTo.addClickHandler(addToHandler);
        return addTo;
    }

    private Button createRemoveMenu() {
        Button remove = new Button("Remove");
        remove.setStyleName("buttonGroupItem");
        return remove;
    }

    private Button createMoveMenu() {
        final Button move = new Button("Move To");
        move.setStyleName("buttonGroupItem");
        move.addStyleName("dropDownMove");
        MenuClickHandler clickHandler = new MenuClickHandler(moveToSelection, move.getElement());
        move.addClickHandler(clickHandler);
        return move;
    }

    public Set<FolderDetails> getAddToDestination() {
        return addToSelection.getSelected();
    }

    public Set<FolderDetails> getMoveToDestination() {
        return this.moveToSelection.getSelected();
    }

    @Override
    public Widget asWidget() {
        return menuHolder;
    }

    public void hidePopup() {
    }

    //
    // inner classes
    //
    private class MenuClickHandler implements ClickHandler {

        private final PopupPanel popup;

        public MenuClickHandler(Widget widget, Element autoHide) {
            this.popup = new PopupPanel();
            this.popup.setStyleName("add_to_popup");
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

        public void hidePopup() { // see ExportAsMenu for usage
            if (this.popup == null || !this.popup.isShowing())
                return;

            this.popup.hide();
        }
    }
}
