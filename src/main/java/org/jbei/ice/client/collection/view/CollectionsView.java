package org.jbei.ice.client.collection.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.add.menu.CreateEntryMenu;
import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.menu.*;
import org.jbei.ice.client.collection.model.PropagateOption;
import org.jbei.ice.client.collection.model.ShareCollectionData;
import org.jbei.ice.client.collection.presenter.MoveToHandler;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.ExportAsOption;
import org.jbei.ice.lib.shared.dto.folder.FolderType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * View for the collections section
 *
 * @author Hector Plahar
 */
public class CollectionsView extends AbstractLayout implements ICollectionView {

    private CollectionMenu systemMenu;
    private CollectionMenu userMenu;
    private CollectionMenu sharedCollections;

    private FlexTable rightContents;

    private CreateEntryMenu createNew;
    private CollectionEntryActionMenu subMenu;
    private BulkEdit bulkEdit;
    private ExportAsMenu exportAs;
    private TransferMenu transferMenu;
    private FeedbackPanel feedback;

    @Override
    protected void initComponents() {
        super.initComponents();
        rightContents = new FlexTable();
        rightContents.setCellPadding(0);
        rightContents.setCellSpacing(0);
        rightContents.setWidth("100%");

        createNew = new CreateEntryMenu("Create Entry");
        feedback = new FeedbackPanel("320px");

        int cell = 0;

        // create new pull down
        rightContents.setWidget(0, cell, createNew);
        rightContents.getFlexCellFormatter().setWidth(0, cell, "120px");

        // sub menu (add to, remove, move to)
        cell += 1;
        subMenu = new CollectionEntryActionMenu();
        rightContents.setWidget(0, cell, subMenu);

        // bulk edit
        String width = (subMenu.getWidth() + 10) + "px";
        rightContents.getFlexCellFormatter().setWidth(0, cell, width);
        bulkEdit = new BulkEdit();
        cell += 1;
        rightContents.setWidget(0, cell, bulkEdit);

        // export as
        rightContents.getFlexCellFormatter().setWidth(0, cell, "97px");
        exportAs = new ExportAsMenu();
        cell += 1;
        rightContents.setWidget(0, cell, exportAs);

        // transfer
        if (ClientController.account.isAdmin()) {
            width = (exportAs.getWidth() + 12) + "px";
            rightContents.getFlexCellFormatter().setWidth(0, cell, width);
            cell += 1;
            transferMenu = new TransferMenu();
            rightContents.setWidget(0, cell, transferMenu);
        }

        // feedback
        cell += 1;
        rightContents.setWidget(0, cell, feedback);
        rightContents.getFlexCellFormatter().setHorizontalAlignment(0, cell, HasAlignment.ALIGN_RIGHT);

        rightContents.setHTML(1, 0, "&nbsp;");
        rightContents.getFlexCellFormatter().setColSpan(1, 0, cell);
    }

    @Override
    protected Widget createContents() {
        FlexTable contents = new FlexTable();
        contents.setWidth("100%");
        contents.setCellSpacing(0);
        contents.setCellPadding(0);

        systemMenu = new SystemCollectionMenu();
        userMenu = new UserCollectionMenu();
        sharedCollections = new SharedCollectionMenu();

        HTMLPanel menuPanel = new HTMLPanel(
                "<br><span id=\"system_menu\"></span><br><span id=\"user_menu\"></span><br><span " +
                        "id=\"shared_collections\"></span><br>");
        menuPanel.add(systemMenu, "system_menu");
        menuPanel.add(userMenu, "user_menu");
        menuPanel.add(sharedCollections, "shared_collections");

        contents.setWidget(0, 0, menuPanel);
        contents.getCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        // separator between menu and content (left and right)
        contents.setHTML(0, 1, "&nbsp;&nbsp;&nbsp;&nbsp;");

        // main content area
        contents.setWidget(0, 2, rightContents);
        contents.getCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);
        contents.getCellFormatter().setHorizontalAlignment(0, 2, HasAlignment.ALIGN_RIGHT);
        contents.getCellFormatter().setWidth(0, 2, "100%");
        return contents;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setMenuDelegates(Delegate<ShareCollectionData> delegate, ServiceDelegate<PropagateOption> propagate) {
        userMenu.setDelegates(delegate, propagate);
    }

    @Override
    public void hideQuickAddInput() {
        this.userMenu.hideQuickText();
    }

    @Override
    public String getCollectionInputValue() {
        return this.userMenu.getQuickAddInputName();
    }

    @Override
    public void setDataView(CollectionDataTable table) {
        rightContents.setWidget(0, 1, subMenu);
        String width = (subMenu.getWidth() + 12) + "px";
        rightContents.getFlexCellFormatter().setWidth(0, 1, width);

        int colspan = rightContents.getCellCount(0);
        rightContents.setWidget(2, 0, table);
        rightContents.getFlexCellFormatter().setColSpan(2, 0, colspan);

        rightContents.setWidget(3, 0, table.getPager());
        rightContents.getFlexCellFormatter().setColSpan(3, 0, colspan);
    }

    @Override
    public void setMainContent(Widget mainContent) {
        rightContents.setWidget(0, 1, subMenu);
        rightContents.setWidget(2, 0, mainContent);
        int colspan = rightContents.getCellCount(0);
        rightContents.getFlexCellFormatter().setColSpan(2, 0, colspan);
        if (rightContents.getRowCount() > 3)
            rightContents.removeRow(3);
    }

    @Override
    public void setCanMove(boolean enableMove) {
        subMenu.setCanMove(enableMove);
    }

    @Override
    public void setSystemCollectionMenuItems(ArrayList<MenuItem> items) {
        this.systemMenu.setMenuItems(items, null);
    }

    @Override
    public void setUserCollectionMenuItems(ArrayList<MenuItem> items, IDeleteMenuHandler handler) {
        this.userMenu.setMenuItems(items, handler);
    }

    @Override
    public void setSharedCollectionsMenuItems(ArrayList<MenuItem> items) {
        this.sharedCollections.setMenuItems(items, null);
    }

    @Override
    public void showFeedbackMessage(String msg, boolean errMsg) {
        if (errMsg)
            feedback.setFailureMessage(msg);
        else {
            feedback.setSuccessMessage(msg);
        }
    }

    @Override
    public SingleSelectionModel<EntryAddType> getAddEntrySelectionHandler() {
        return this.createNew.getSelectionModel();
    }

    @Override
    public boolean getQuickEditVisibility() {
        return this.userMenu.isQuickEditVisible();
    }

    @Override
    public void addQuickAddKeyHandler(KeyPressHandler handler) {
        this.userMenu.addQuickAddKeyPressHandler(handler);
    }

    @Override
    public void addQuickAddHandler(ClickHandler handler) {
        this.userMenu.addQuickAddHandler(handler);
    }

    @Override
    public void updateMenuItemCounts(ArrayList<MenuItem> item) {
        this.userMenu.updateCounts(item);
    }

    @Override
    public void setBusyIndicator(Set<Long> ids, boolean visible) {
        this.userMenu.setBusyIndicator(ids, visible);
    }

    @Override
    public void addMenuItem(MenuItem item, IDeleteMenuHandler handler) {
        this.userMenu.addMenuItem(item, handler);
    }

    @Override
    public void addQuickEditKeyPressHandler(KeyPressHandler handler) {
        this.userMenu.addQuickEditKeyPressHandler(handler);
    }

    @Override
    public void setMenuItem(MenuItem item, IDeleteMenuHandler handler) {
        this.userMenu.setMenuItem(item, handler);
    }

    @Override
    public void setPromotionDelegate(ServiceDelegate<MenuItem> delegate) {
        this.sharedCollections.setPromotionDelegate(delegate);
        this.userMenu.setPromotionDelegate(delegate);
    }

    @Override
    public void setDemotionDelegate(ServiceDelegate<MenuItem> delegate) {
        this.systemMenu.setDemotionDelegate(delegate);
    }

    @Override
    public void setCurrentMenuSelection(long id) {
        this.userMenu.setSelection(id);
        this.systemMenu.setSelection(id);
        this.sharedCollections.setSelection(id);
    }

    @Override
    public String getQuickEditInput() {
        return this.userMenu.getQuickEditText();
    }

    @Override
    public MenuItem getCurrentMenuEditSelection() {
        return this.userMenu.getCurrentEditSelection();
    }

    @Override
    public SingleSelectionModel<MenuItem> getMenuModel(FolderType type) {
        switch (type) {
            case PUBLIC:
                return systemMenu.getSelectionModel();
            case PRIVATE:
                return userMenu.getSelectionModel();
            case SHARED:
                return sharedCollections.getSelectionModel();
        }
        return null;
    }

    @Override
    public SingleSelectionModel<ExportAsOption> getExportAsModel() {
        return exportAs.getSelectionModel();
    }

    @Override
    public void enableBulkEdit(boolean enable) {
        this.rightContents.getFlexCellFormatter().setVisible(0, 2, true);
        this.bulkEdit.setEnabled(enable);
    }

    @Override
    public void enableBulkEditVisibility(boolean b) {
        this.rightContents.getFlexCellFormatter().setVisible(0, 2, b);
    }

    @Override
    public void enableExportAs(boolean enable) {
        this.exportAs.enable(enable);
        if (transferMenu != null)
            this.transferMenu.setEnabled(enable);
        this.subMenu.setCanAdd(enable);
    }

    @Override
    public void addSubMenuFolder(OptionSelect option) {
        subMenu.addOption(option);
    }

    @Override
    public void addAddToSubmitHandler(SubmitHandler handler) {
        subMenu.addAddToSubmitHandler(handler);
    }

    @Override
    public void addTransferHandler(ClickHandler handler) {
        if (transferMenu != null)
            this.transferMenu.setHandler(handler);
    }

    @Override
    public void setTransferOptions(ArrayList<OptionSelect> options) {
        if (transferMenu != null)
            transferMenu.setOptions(options);
    }

    @Override
    public ArrayList<OptionSelect> getSelectedTransfers() {
        if (transferMenu != null)
            return transferMenu.getSelectedItems();
        return null;
    }

    @Override
    public void addMoveSubmitHandler(MoveToHandler moveHandler) {
        subMenu.setMoveToSubmitHandler(moveHandler);
    }

    @Override
    public void addRemoveHandler(ClickHandler handler) {
        subMenu.addRemoveHandler(handler);
    }

    @Override
    public void addBulkEditHandler(ClickHandler handler) {
        bulkEdit.setClickHandler(handler);
    }

    @Override
    public void updateSubMenuFolder(OptionSelect optionSelect) {
        subMenu.updateOption(optionSelect);
    }

    @Override
    public void removeSubMenuFolder(OptionSelect optionSelect) {
        subMenu.removeOption(optionSelect);
    }

    @Override
    public List<OptionSelect> getSelectedOptions(boolean addOption) {
        return subMenu.getSelectedOptions(addOption);
    }
}
