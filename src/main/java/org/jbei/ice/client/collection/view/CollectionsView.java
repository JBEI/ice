package org.jbei.ice.client.collection.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.add.menu.CreateEntryMenu;
import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.menu.CollectionEntryActionMenu;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.collection.menu.ExportAsMenu;
import org.jbei.ice.client.collection.menu.ExportAsOption;
import org.jbei.ice.client.collection.menu.IDeleteMenuHandler;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.presenter.MoveToHandler;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class CollectionsView extends AbstractLayout implements ICollectionView {

    private CollectionMenu systemMenu;
    private CollectionMenu userMenu;

    private FlexTable contents;
    private FlexTable rightContents;

    private CreateEntryMenu createNew;
    private CollectionEntryActionMenu subMenu;
    private ExportAsMenu exportAs;
    private FeedbackPanel feedback;

    @Override
    protected void initComponents() {
        super.initComponents();
        rightContents = new FlexTable();
        rightContents.setCellPadding(0);
        rightContents.setCellSpacing(0);
        rightContents.setWidth("100%");

        createNew = new CreateEntryMenu();
        feedback = new FeedbackPanel("400px");

        // create new pull down
        rightContents.setWidget(0, 0, createNew);
        rightContents.getFlexCellFormatter().setWidth(0, 0, "120px");

        // sub menu (add to, remove, move to)
        subMenu = new CollectionEntryActionMenu();
        rightContents.setWidget(0, 1, subMenu);
        String width = (subMenu.getWidth() + 12) + "px";
        rightContents.getFlexCellFormatter().setWidth(0, 1, width);

        // export as
        exportAs = new ExportAsMenu();
        rightContents.setWidget(0, 2, exportAs);

        // feedback
        rightContents.setWidget(0, 3, feedback);
        rightContents.getFlexCellFormatter().setHorizontalAlignment(0, 3, HasAlignment.ALIGN_RIGHT);

        rightContents.setHTML(1, 0, "&nbsp;");
        rightContents.getFlexCellFormatter().setColSpan(1, 0, 3);
    }

    @Override
    protected Widget createContents() {
        contents = new FlexTable();
        contents.setWidth("100%");
        contents.setCellSpacing(0);
        contents.setCellPadding(0);

        systemMenu = new CollectionMenu(false, "Collections");
        userMenu = new CollectionMenu(true, "My Collections");
        HTMLPanel menuPanel = new HTMLPanel(
                "<span id=\"system_menu\"></span><br><span id=\"user_menu\"></span><br>");
        menuPanel.add(systemMenu, "system_menu");
        menuPanel.add(userMenu, "user_menu");

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
    public void hideQuickAddInput() {
        this.userMenu.hideQuickText();
    }

    @Override
    public String getCollectionInputValue() {
        return this.userMenu.getQuickAddBox().getValue();
    }

    @Override
    public void setDataView(CollectionDataTable table) {
        rightContents.setWidget(0, 1, subMenu);
        String width = (subMenu.getWidth() + 12) + "px";
        rightContents.getFlexCellFormatter().setWidth(0, 1, width);

        rightContents.setWidget(2, 0, table);
        rightContents.getFlexCellFormatter().setColSpan(2, 0, 4);

        // table pager TODO : this should be merged with the table
        rightContents.setWidget(3, 0, table.getPager());
        rightContents.getFlexCellFormatter().setColSpan(3, 0, 4);
    }

    @Override
    public void setMainContent(Widget mainContent) {
        rightContents.setWidget(0, 1, subMenu);
        rightContents.setWidget(2, 0, mainContent);
        rightContents.getFlexCellFormatter().setColSpan(2, 0, 4);
        if (rightContents.getRowCount() > 3)
            rightContents.removeRow(3);
    }

    @Override
    public void setSubMenuEnable(boolean enableAddTo, boolean enableRemove, boolean enableMoveTo) {
        subMenu.setEnable(enableAddTo, enableRemove, enableMoveTo);
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
    public void showFeedbackMessage(String msg, boolean errMsg) {
        if (errMsg)
            feedback.setFailureMessage(msg);
        else
            feedback.setSuccessMessage(msg);

        new Timer() {
            @Override
            public void run() {
                feedback.setVisible(false);
            }
        }.schedule(10000);
    }

    @Override
    public SingleSelectionModel<EntryAddType> getAddEntrySelectionHandler() {
        return this.createNew.getSelectionModel();
    }

    @Override
    public void setQuickAddVisibility(boolean visible) {
        this.userMenu.getQuickAddBox().setVisible(visible);
    }

    @Override
    public boolean getQuickAddVisibility() {
        return this.userMenu.getQuickAddBox().isVisible();
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
    public void updateMenuItemCounts(ArrayList<MenuItem> item) {
        this.userMenu.updateCounts(item);
    }

    @Override
    public void setBusyIndicator(Set<Long> ids) {
        this.userMenu.setBusyIndicator(ids);
    }

    @Override
    public void addMenuItem(MenuItem item, IDeleteMenuHandler handler) {
        this.userMenu.addMenuItem(item, handler);
    }

    @Override
    public void addQuickEditKeyDownHandler(KeyDownHandler handler) {
        this.userMenu.addQuickEditKeyDownHandler(handler);
    }

    @Override
    public void addQuickEditBlurHandler(BlurHandler handler) {
        this.userMenu.addQuickEditBlurHandler(handler);
    }

    @Override
    public void setMenuItem(MenuItem item, IDeleteMenuHandler handler) {
        this.userMenu.setMenuItem(item, handler);
    }

    @Override
    public void setCurrentMenuSelection(long id) {
        this.userMenu.setSelection(id);
        this.systemMenu.setSelection(id);
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
    public SingleSelectionModel<MenuItem> getUserMenuModel() {
        return userMenu.getSelectionModel();
    }

    @Override
    public SingleSelectionModel<MenuItem> getSystemMenuModel() {
        return systemMenu.getSelectionModel();
    }

    @Override
    public SingleSelectionModel<ExportAsOption> getExportAsModel() {
        return exportAs.getSelectionModel();
    }

    @Override
    public void enableExportAs(boolean enable) {
        this.exportAs.enable(enable);
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
    public void addMoveSubmitHandler(MoveToHandler moveHandler) {
        subMenu.setMoveToSubmitHandler(moveHandler);
    }

    @Override
    public void addRemoveHandler(ClickHandler handler) {
        subMenu.addRemoveHandler(handler);
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
