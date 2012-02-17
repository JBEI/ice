package org.jbei.ice.client.collection;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.table.CollectionEntriesDataTable;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Interface for view that displays details of entry collections
 * 
 * @author Hector Plahar
 */

public interface ICollectionEntriesView {

    void setSystemCollectionMenuItems(ArrayList<MenuItem> items);

    void setUserCollectionMenuItems(ArrayList<MenuItem> items);

    void setQuickAddVisibility(boolean visible);

    boolean getQuickAddVisibility();

    boolean getQuickEditVisibility();

    // key handler for the quick add text box
    void addQuickAddKeyHandler(KeyPressHandler handler);

    // handler for what happens when the user pressed "enter" in the quick edit input
    void addQuickEditKeyDownHandler(KeyDownHandler handler);

    // handler for on blur on the quick edit box. should be the same as pressing enter
    void addQuickEditBlurHandler(BlurHandler handler);

    // user entered collection name
    String getCollectionInputValue();

    // set menu items indicated by the ids with the busy indicator
    // currently supported by the user menu only
    void setBusyIndicator(Set<String> ids);

    void updateMenuItemCounts(ArrayList<MenuItem> item);

    // add menu item to user menu
    void addMenuItem(MenuItem item);

    // sets the menu item in a menu list
    void setMenuItem(MenuItem item);

    void setCurrentMenuSelection(long id);

    // new name for existing menu item
    String getQuickEditInput();

    // user menu selection for edit
    MenuItem getCurrentMenuEditSelection();

    void hideQuickAddInput();

    Widget asWidget();

    void setCollectionSubMenu(Widget widget);

    SingleSelectionModel<EntryAddType> getAddEntrySelectionHandler();

    /**
     * active data view
     * 
     * @param table
     *            data view table. depends on user selection
     */
    void setDataView(CollectionEntriesDataTable table);

    void setMainContent(Widget mainContent);

    // TODO : the following needs to be in abstract layout
    void setFeedback(Widget feedback);

    SingleSelectionModel<MenuItem> getUserMenuModel();

    SingleSelectionModel<MenuItem> getSystemMenuModel();
}
