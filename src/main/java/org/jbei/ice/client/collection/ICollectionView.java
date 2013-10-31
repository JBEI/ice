package org.jbei.ice.client.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.menu.IDeleteMenuHandler;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.model.PropagateOption;
import org.jbei.ice.client.collection.model.ShareCollectionData;
import org.jbei.ice.client.collection.presenter.MoveToHandler;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.ExportAsOption;
import org.jbei.ice.lib.shared.dto.folder.FolderType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Interface for view that displays details of entry collections
 *
 * @author Hector Plahar
 */

public interface ICollectionView {

    void setSystemCollectionMenuItems(ArrayList<MenuItem> items);

    void setUserCollectionMenuItems(ArrayList<MenuItem> items, IDeleteMenuHandler handler);

    boolean getQuickEditVisibility();

    // key handler for the quick add text box
    void addQuickAddKeyHandler(KeyPressHandler handler);

    // handler for what happens when the user pressed "enter" in the quick edit input
    void addQuickEditKeyPressHandler(KeyPressHandler handler);

    // user entered collection name
    String getCollectionInputValue();

    // set menu items indicated by the ids with the busy indicator
    // currently supported by the user menu only
    void setBusyIndicator(Set<Long> ids, boolean visible);

    void updateMenuItemCounts(ArrayList<MenuItem> item);

    // add menu item to user menu
    void addMenuItem(MenuItem item, IDeleteMenuHandler handler);

    // sets the menu item in a menu list
    void setMenuItem(MenuItem item, IDeleteMenuHandler handler);

    void setCurrentMenuSelection(long id);

    // new name for existing menu item
    String getQuickEditInput();

    MenuItem getCurrentMenuEditSelection();

    void hideQuickAddInput();

    Widget asWidget();

    SingleSelectionModel<EntryAddType> getAddEntrySelectionHandler();

    /**
     * active data view
     *
     * @param table data view table. depends on user selection
     */
    void setDataView(CollectionDataTable table);

    void setMainContent(Widget mainContent);

    void showFeedbackMessage(String msg, boolean errMsg);

    SingleSelectionModel<MenuItem> getMenuModel(FolderType type);

    void addSubMenuFolder(OptionSelect option);

    // called when a folder is updated
    void updateSubMenuFolder(OptionSelect optionSelect);

    // called when a folder is deleted
    void removeSubMenuFolder(OptionSelect optionSelect);

    List<OptionSelect> getSelectedOptions(boolean addToOption);

    void addAddToSubmitHandler(SubmitHandler handler);

    void addMoveSubmitHandler(MoveToHandler moveHandler);

    void addRemoveHandler(ClickHandler handler);

    void addBulkEditHandler(ClickHandler handler);

    void setCanMove(boolean enableMove);

    SingleSelectionModel<ExportAsOption> getExportAsModel();

    void enableExportAs(boolean enable);

    void enableBulkEdit(boolean enable);

    void enableBulkEditVisibility(boolean b);

    void setSharedCollectionsMenuItems(ArrayList<MenuItem> items);

    void setMenuDelegates(Delegate<ShareCollectionData> delegate, ServiceDelegate<PropagateOption> propagate);

    void addTransferHandler(ClickHandler handler);

    void setTransferOptions(ArrayList<OptionSelect> options);

    ArrayList<OptionSelect> getSelectedTransfers();

    void setPromotionDelegate(ServiceDelegate<MenuItem> delegate);

    void setDemotionDelegate(ServiceDelegate<MenuItem> delegate);

    void setPublicAccessDelegate(ServiceDelegate<HashMap<Long, Boolean>> delegate);

    void addQuickAddHandler(ClickHandler handler);
}
