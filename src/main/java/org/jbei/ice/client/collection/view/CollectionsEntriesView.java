package org.jbei.ice.client.collection.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jbei.ice.client.collection.ICollectionEntriesView;
import org.jbei.ice.client.collection.add.menu.CreateEntryMenu;
import org.jbei.ice.client.collection.event.SubmitHandler;
import org.jbei.ice.client.collection.menu.CollectionEntryActionMenu;
import org.jbei.ice.client.collection.menu.CollectionMenu;
import org.jbei.ice.client.collection.menu.ExportAsMenu;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.presenter.MoveToSubmitHandler;
import org.jbei.ice.client.collection.table.CollectionEntriesDataTable;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class CollectionsEntriesView extends AbstractLayout implements ICollectionEntriesView {

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
        feedback = new FeedbackPanel("450px");

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

        // systems collections menu
        systemMenu = new CollectionMenu(false, "Collections");
        contents.setWidget(0, 0, systemMenu);
        contents.getCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        // separator between menus (top and bottom)
        contents.setHTML(1, 0, "&nbsp;");

        // user collection menu
        userMenu = new CollectionMenu(true, "My Collections");
        contents.setWidget(2, 0, userMenu);
        contents.getCellFormatter().setVerticalAlignment(2, 0, HasAlignment.ALIGN_TOP);

        // separator between menu and content (left and right)
        contents.setHTML(0, 1, "&nbsp;&nbsp;&nbsp;&nbsp;");
        contents.getFlexCellFormatter().setRowSpan(0, 1, 3);

        // main content area
        contents.setWidget(0, 2, rightContents);
        contents.getFlexCellFormatter().setRowSpan(0, 2, 3);
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
    public void setDataView(CollectionEntriesDataTable table) {
        feedback.setVisible(false);

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
    public void setMainContent(Widget mainContent, boolean showSubMenu) {
        feedback.setVisible(false);

        if (showSubMenu)
            rightContents.setWidget(0, 1, subMenu);
        else
            rightContents.setHTML(0, 1, "&nbsp;");

        rightContents.setWidget(2, 0, mainContent);
        rightContents.getFlexCellFormatter().setColSpan(2, 0, 4);
        if (rightContents.getRowCount() > 3)
            rightContents.removeRow(3);
    }

    @Override
    public void setSystemCollectionMenuItems(ArrayList<MenuItem> items) {
        this.systemMenu.setMenuItems(items);
    }

    @Override
    public void setUserCollectionMenuItems(ArrayList<MenuItem> items) {
        this.userMenu.setMenuItems(items);
    }

    @Override
    public void showFeedbackMessage(String msg, boolean errMsg) {
        if (errMsg)
            feedback.setFailureMessage(msg);
        else
            feedback.setSuccessMessage(msg);
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
    public void addMenuItem(MenuItem item) {
        this.userMenu.addMenuItem(item);
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
    public void setMenuItem(MenuItem item) {
        this.userMenu.setMenuItem(item);
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
    public void addSubMenuFolder(OptionSelect option) {
        subMenu.addOption(option);
    }

    @Override
    public void addAddToSubmitHandler(SubmitHandler handler) {
        subMenu.addAddToSubmitHandler(handler);
    }

    @Override
    public void addMoveSubmitHandler(MoveToSubmitHandler moveHandler) {
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
