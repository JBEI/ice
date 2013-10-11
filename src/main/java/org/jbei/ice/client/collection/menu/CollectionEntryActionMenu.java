package org.jbei.ice.client.collection.menu;

import java.util.List;

import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.presenter.MoveToHandler;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sub-menu for manipulating collection of entries.
 * Actions are "Add To", "Remove" and "Move To"
 *
 * @author Hector Plahar
 */
public class CollectionEntryActionMenu implements IsWidget {

    /**
     * Resources to access images and styles
     */
    public interface Resources extends ClientBundle {

        static Resources INSTANCE = GWT.create(Resources.class);

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

        String buttonGroupItem();

        String buttonAddTo();

        String buttonRemove();

        String buttonMoveTo();
    }

    private final FlexTable menuHolder;
    private final AddToMenuItem<OptionSelect> add;
    private final Button removeButton;
    private final AddToMenuItem<OptionSelect> move;
    private static final int WIDTH = 240;

    public CollectionEntryActionMenu() {
        this.menuHolder = new FlexTable();
        this.menuHolder.setWidth(WIDTH + "px");
        this.menuHolder.setCellPadding(0);
        this.menuHolder.setCellSpacing(0);

        this.add = new AddToMenuItem<OptionSelect>("<i class=\"" + FAIconType.PLUS.getStyleName()
                                                           + "\" style=\"opacity:0.85;\"></i> Add To");
        this.add.setStyleName(Resources.INSTANCE.subMenuStyle().buttonGroupItem());
        this.add.addStyleName(Resources.INSTANCE.subMenuStyle().buttonAddTo());
        this.menuHolder.setWidget(0, 0, add);
        this.add.setEnabled(false);

        this.removeButton = createRemoveMenu();
        this.menuHolder.setWidget(0, 1, removeButton);
        this.removeButton.setEnabled(false);

        this.move = new AddToMenuItem<OptionSelect>("<i class=\"" + FAIconType.MOVE.getStyleName()
                                                            + "\" style=\"opacity:0.85;\"></i> Move To");
        this.move.setStyleName(Resources.INSTANCE.subMenuStyle().buttonGroupItem());
        this.move.addStyleName(Resources.INSTANCE.subMenuStyle().buttonMoveTo());
        this.menuHolder.setWidget(0, 2, move);
        this.move.setEnabled(false);

        Resources.INSTANCE.subMenuStyle().ensureInjected();
    }

    private Button createRemoveMenu() {
        Button remove = new Button("<i class=\"" + FAIconType.MINUS.getStyleName()
                                           + "\" style=\"opacity:0.85;\"></i> Remove");
        remove.setStyleName(Resources.INSTANCE.subMenuStyle().buttonGroupItem());
        remove.addStyleName(Resources.INSTANCE.subMenuStyle().buttonRemove());
        return remove;
    }

    public int getWidth() {
        return WIDTH;
    }

    @Override
    public Widget asWidget() {
        return menuHolder;
    }

    public void addAddToSubmitHandler(SubmitHandler handler) {
        this.add.addSubmitHandler(handler);
    }

    /**
     * Adds information about a collection that the user can add or move
     * part to/from
     *
     * @param option id and name of collection
     */
    public void addOption(OptionSelect option) {
        this.add.addOption(option);
        this.move.addOption(option);
    }

    public List<OptionSelect> getSelectedOptions(boolean add) {
        if (add)
            return this.add.getSelectedItems();
        return this.move.getSelectedItems();
    }

    public void updateOption(OptionSelect optionSelect) {
        this.add.updateOption(optionSelect);
        this.move.updateOption(optionSelect);
    }

    public void removeOption(OptionSelect option) {
        this.add.removeOption(option);
        this.move.removeOption(option);
    }

    public void setMoveToSubmitHandler(MoveToHandler moveHandler) {
        this.move.addSubmitHandler(moveHandler);
    }

    public void addRemoveHandler(ClickHandler handler) {
        this.removeButton.addClickHandler(handler);
    }

    /**
     * enables or disables the moveTo and remove button.
     * Both actions move entries out of current folder
     *
     * @param canMove whether user can move entries out of this folder
     */
    public void setCanMove(boolean canMove) {
        this.removeButton.setEnabled(canMove);
        this.move.setEnabled(canMove);
    }

    /**
     * enables or disables the add button.
     *
     * @param canAdd whether user can add current entry to another folder or not
     */
    public void setCanAdd(boolean canAdd) {
        this.add.setEnabled(canAdd);
    }
}