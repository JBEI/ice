package org.jbei.ice.client.collection.menu;

import java.util.List;

import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.presenter.MoveToHandler;
import org.jbei.ice.client.collection.view.OptionSelect;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
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
public class CollectionEntryActionMenu implements IsWidget {

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

    private final AddToMenuItem<OptionSelect> add;
    private final Button removeButton;
    private final AddToMenuItem<OptionSelect> move;
    private final static int WIDTH = 240;

    public CollectionEntryActionMenu() {
        this.menuHolder = new FlexTable();
        this.menuHolder.setStyleName("button_group");
        this.menuHolder.setWidth(WIDTH + "px");
        this.menuHolder.setCellPadding(0);
        this.menuHolder.setCellSpacing(0);

        add = new AddToMenuItem<OptionSelect>("Add To", true);
        this.menuHolder.setWidget(0, 0, add);

        removeButton = createRemoveMenu();
        this.menuHolder.setWidget(0, 1, removeButton);

        move = new AddToMenuItem<OptionSelect>("Move To", false);
        this.menuHolder.setWidget(0, 2, move);
        Resources.INSTANCE.subMenuStyle().ensureInjected();
    }

    private Button createRemoveMenu() {
        Button remove = new Button("Remove");
        remove.setStyleName("button_group_item");
        remove.addStyleName(Resources.INSTANCE.subMenuStyle().subMenuRemove());
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

    public void addOption(OptionSelect option) {
        this.add.addOption(option);
        this.move.addOption(option);
    }

    public void setOptions(List<OptionSelect> options) {
        this.add.setOptions(options);
        this.move.setOptions(options);
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

    public void setEnable(boolean enableAddTo, boolean enableRemove, boolean enableMoveTo) {
        this.add.setEnabled(enableAddTo);
        removeButton.setEnabled(enableRemove);
        this.move.setEnabled(enableMoveTo);
    }
}