package org.jbei.ice.client.collection.menu;

import java.util.Set;

import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;
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

        static Resources INSTANCE = GWT.create(Resources.class);

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
    private PopupHandler addToHandler;
    private PopupHandler clickHandler;

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
        Resources.INSTANCE.subMenuStyle().ensureInjected();
    }

    private Button createAddToButton() {
        final Button addTo = new Button("Add To");
        addTo.setStyleName("buttonGroupItem");
        addTo.addStyleName("firstItem");
        addTo.addStyleName(Resources.INSTANCE.subMenuStyle().dropDownAdd());
        addToHandler = new PopupHandler(addToSelection, addTo.getElement(), -1, 1);
        addTo.addClickHandler(addToHandler);
        return addTo;
    }

    private Button createRemoveMenu() {
        Button remove = new Button("Remove");
        remove.setStyleName("buttonGroupItem");
        remove.addStyleName(Resources.INSTANCE.subMenuStyle().subMenuRemove());
        return remove;
    }

    private Button createMoveMenu() {
        final Button move = new Button("Move To");
        move.setStyleName("buttonGroupItem");
        move.addStyleName(Resources.INSTANCE.subMenuStyle().dropDownMove());
        clickHandler = new PopupHandler(moveToSelection, move.getElement(), -1, 1);
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
        addToHandler.hidePopup();
        clickHandler.hidePopup();
    }
}
