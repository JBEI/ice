package org.jbei.ice.client.collection.menu;

import java.util.Set;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
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

    /**
     * Resources to access images and styles
     */
    public interface Resources extends ClientBundle {

        @Source("org/jbei/ice/client/resource/image/arrow_down.png")
        @ImageOptions(repeatStyle = RepeatStyle.None)
        ImageResource sortDown();

        /**
         * The styles used in this widget.
         */
        @Source(Style.DEFAULT_CSS)
        Style subMenuStyle();
    }

    /**
     * Styles used by this widget.
     */

    @ImportedWithPrefix("")
    interface Style extends CssResource {
        /**
         * The path to the default CSS styles used by this resource.
         */
        String DEFAULT_CSS = "org/jbei/ice/client/resource/css/SubMenu.css";

        String dropDownAdd();

        String dropDownMove();

        String subMenuRemove();
    }

    private final FlexTable menuHolder;

    private final UserCollectionMultiSelect addToSelection;
    private final UserCollectionMultiSelect moveToSelection;
    private final Button addToButton;
    private final Button removeButton;
    private final Button moveToButton;
    private static Resources resource = GWT.create(Resources.class);

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
        resource.subMenuStyle().ensureInjected();
    }

    private Button createAddToButton() {
        final Button addTo = new Button("Add To");
        addTo.setStyleName("buttonGroupItem");
        addTo.addStyleName("firstItem");
        addTo.addStyleName(resource.subMenuStyle().dropDownAdd());
        MenuClickHandler addToHandler = new MenuClickHandler(addToSelection, addTo.getElement());
        addTo.addClickHandler(addToHandler);
        return addTo;
    }

    private Button createRemoveMenu() {
        Button remove = new Button("Remove");
        remove.setStyleName("buttonGroupItem");
        remove.addStyleName(resource.subMenuStyle().subMenuRemove());
        return remove;
    }

    private Button createMoveMenu() {
        final Button move = new Button("Move To");
        move.setStyleName("buttonGroupItem");
        move.addStyleName(resource.subMenuStyle().dropDownMove());
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
        // TODO
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
