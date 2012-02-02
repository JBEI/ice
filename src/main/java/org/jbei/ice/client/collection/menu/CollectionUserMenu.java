package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Left bar menu for showing user collections. Also adds widgets such as
 * an icon for adding a new user collection and edit/delete
 * 
 * TODO : this could probably extend CollectionEntryMenu or a common
 * TODO : abstract parent class extracted from both
 * 
 * @author Hector Plahar
 */
public class CollectionUserMenu extends Composite implements HasClickHandlers {

    interface Resources extends ClientBundle {

        static Resources INSTANCE = GWT.create(Resources.class);

        @Source("org/jbei/ice/client/resource/image/plus.png")
        ImageResource plusImage();

        @Source("org/jbei/ice/client/resource/image/minus.png")
        ImageResource minusImage();

        @Source("org/jbei/ice/client/resource/image/edit.png")
        ImageResource editImage();

        @Source("org/jbei/ice/client/resource/image/delete.png")
        ImageResource deleteImage();

        @Source("org/jbei/ice/client/resource/image/busy.gif")
        ImageResource busyIndicatorImage();
    }

    private final FlexTable table;
    private FolderDetails currentSelected;
    private FolderDetails currentEditSelection;
    private final TextBox quickAddBox;
    private final Image quickAddButton;
    private int row;
    private final TextBox editCollectionNameBox;
    private int editRow = -1;
    private int editIndex = -1;

    public CollectionUserMenu() {

        table = new FlexTable();
        initWidget(table);

        // quick edit
        editCollectionNameBox = new TextBox();
        editCollectionNameBox.setStyleName("input_box");
        editCollectionNameBox.setWidth("99%");
        editCollectionNameBox.setVisible(false);

        // quick add widgets
        quickAddBox = new TextBox();
        quickAddBox.setStyleName("input_box");
        quickAddBox.setText("Enter new collection name...");
        quickAddBox.setWidth("99%");
        quickAddButton = new Image(Resources.INSTANCE.plusImage());
        quickAddButton.setStyleName("collection_quick_add_image");

        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("collection_menu_table");

        String html = "<span>MY COLLECTIONS</span><span style=\"float: right\" id=\"quick_add\"></span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.add(quickAddButton, "quick_add");
        table.setWidget(row, 0, panel);
        table.getFlexCellFormatter().setStyleName(row, 0, "collections_menu_header");

        // add quick add box
        row += 1;
        table.setWidget(row, 0, quickAddBox);
        initComponents();
    }

    private void initComponents() {
        quickAddButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                switchButton();
            }
        });
    }

    public TextBox getQuickEditBox() {
        return this.editCollectionNameBox;
    }

    public void showFolderCount(FolderDetails details) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;

            if (details.getId() == cell.getFolderId()) {
                table.setWidget(i, 0, cell);
                cell.showFolderCount();
            }
        }
    }

    public void setFolderDetails(ArrayList<FolderDetails> folders) {
        if (folders == null || folders.isEmpty())
            return;

        for (FolderDetails folder : folders) {
            addFolderDetail(folder);
        }
    }

    /**
     * checks if the user clicked within the menu contents
     * and not, for eg. the header
     * 
     * @param event
     *            user click event
     * @return true if a response is required for user selection
     */
    public boolean isValidClick(ClickEvent event) {
        if (event == null)
            return false;

        Cell cell = this.table.getCellForEvent(event);
        if (cell == null)
            return false;

        boolean isValid = (cell.getCellIndex() != 0 || cell.getRowIndex() != 0);
        if (!isValid)
            return isValid;

        if (quickAddBox.isVisible())
            isValid = (cell.getRowIndex() != 1);

        return isValid;
    }

    /**
     * replaces current edit cell (in menu)
     * with new cell with folder
     * 
     * @param folder
     *            new folder for cell
     */
    public void setEditDetail(FolderDetails folder) {
        if (this.editIndex == -1 && this.editRow == -1)
            return;

        if (folder == null)
            return;

        final MenuCell cell = new MenuCell(folder);
        cell.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                currentSelected = cell.getFolder();
            }
        });
        table.setWidget(editRow, editIndex, cell);
        this.editCollectionNameBox.setVisible(false);
    }

    public void addFolderDetail(FolderDetails folder) {
        if (folder == null)
            return;

        final MenuCell cell = new MenuCell(folder);
        cell.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                currentSelected = cell.getFolder();
            }
        });

        row += 1;
        table.setWidget(row, 0, cell);
    }

    /**
     * sets the busy indicator where the folder counts are displayed
     * to indicate that some form of update is taking place
     * 
     * @param folders
     */
    public void setBusyIndicator(Set<FolderDetails> folders) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;
            if (folders.contains(cell.getFolder()))
                cell.showBusyIndicator();
        }
    }

    public void updateCounts(ArrayList<FolderDetails> folders) {
        for (int i = 0; i < table.getRowCount(); i += 1) {
            Widget w = table.getWidget(i, 0);
            if (!(w instanceof MenuCell))
                continue;

            MenuCell cell = (MenuCell) w;

            // TODO highly inefficient
            for (FolderDetails folder : folders) {
                if (folder.getId() == cell.getFolderId()) {
                    cell.updateCount(folder.getCount());
                    cell.showFolderCount();
                    break;
                }
            }
        }
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    public Widget getQuickAddButton() {
        return this.quickAddButton;
    }

    public TextBox getQuickAddBox() {
        return this.quickAddBox;
    }

    public FolderDetails getCurrentSelection() {
        return currentSelected;
    }

    public FolderDetails getCurrentEditSelection() {
        return currentEditSelection;
    }

    public void switchButton() {
        if (quickAddBox.isVisible()) {
            quickAddButton.setUrl(Resources.INSTANCE.plusImage().getSafeUri());
            quickAddButton.setStyleName("collection_quick_add_image");
            quickAddBox.setVisible(false);
            quickAddBox.setStyleName("input_box");
        } else {
            quickAddButton.setUrl(Resources.INSTANCE.minusImage().getSafeUri());
            quickAddButton.setStyleName("collection_quick_add_image");
            quickAddBox.setText("");
            quickAddBox.setVisible(true);
            quickAddBox.setFocus(true);
        }
    }

    public void hideQuickText() {
        quickAddButton.setUrl(Resources.INSTANCE.plusImage().getSafeUri());
        quickAddButton.setStyleName("collection_quick_add_image");
        quickAddBox.setVisible(false);
        quickAddBox.setStyleName("input_box");
    }

    // inner class
    private class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final FolderDetails folder;
        private final String html;
        private final Image busy;

        private Label count;
        private final HoverCell action;
        private final String folderId;

        public MenuCell(final FolderDetails folder) {

            super.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
            // text box used when user wishes to edit a collection name

            this.folder = folder;
            folderId = "right" + folder.getId();
            action = new HoverCell();
            action.getEdit().addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    // TODO : probably should be moved outside of this class
                    event.stopPropagation();
                    Cell cell = table.getCellForEvent(event);
                    editCollectionNameBox.setText(folder.getName());
                    editRow = cell.getRowIndex();
                    editIndex = cell.getCellIndex();
                    currentEditSelection = folder;
                    table.setWidget(cell.getRowIndex(), cell.getCellIndex(), editCollectionNameBox);
                    editCollectionNameBox.setVisible(true);
                    editCollectionNameBox.setFocus(true);
                }
            });

            html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + folder.getName() + "</span><span class=\"menu_count\" id=\"" + folderId
                    + "\"></span>";

            panel = new HTMLPanel(html);

            count = new Label(formatNumber(folder.getCount()));

            panel.add(count, folderId);
            panel.setStyleName("collection_user_menu_row");
            initWidget(panel);

            // init busy indicator
            busy = new Image(Resources.INSTANCE.busyIndicatorImage());
        }

        public void showBusyIndicator() {
            setRightPanel(busy);
        }

        public void showFolderCount() {
            setRightPanel(count);
        }

        public void updateCount(long newCount) {
            folder.setCount(newCount);
            count = new Label(formatNumber(folder.getCount()));
        }

        public long getFolderId() {
            return this.folder.getId();
        }

        public FolderDetails getFolder() {
            return this.folder;
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);

            switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEOVER:
                setRightPanel(action);
                break;

            case Event.ONMOUSEOUT:
                EventTarget target = event.getRelatedEventTarget(); // image

                if (Element.is(target)) {
                    Element element = Element.as(target);
                    Element eDelete = action.getDelete().getElement();
                    Element eEdit = action.getEdit().getElement();

                    if (element.equals(eEdit) || element.equals(eDelete))
                        break;
                }
                setRightPanel(count);
                break;
            }
        }

        private void setRightPanel(Widget widget) {
            Widget toReplace = panel.getWidget(0);
            if (toReplace == null)
                Window.alert("Cannot replace widget");
            else {
                panel.remove(0);
                panel.add(widget, folderId);
            }
        }

        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }

        private String formatNumber(long l) {
            NumberFormat format = NumberFormat.getFormat("##,###");
            return format.format(l);
        }
    }

    private class HoverCell extends Composite {
        private final HorizontalPanel panel;
        private final Image edit;
        private final Image delete;

        public HoverCell() {

            panel = new HorizontalPanel();
            panel.setStyleName("user_collection_action");
            initWidget(panel);

            edit = new Image(Resources.INSTANCE.editImage());
            delete = new Image(Resources.INSTANCE.deleteImage());

            panel.add(edit);
            panel.setHeight("16px");
            HTML pipe = new HTML("&nbsp;|&nbsp;");
            pipe.addStyleName("color_eee");
            panel.add(pipe);
            panel.add(delete);
            panel.setStyleName("menu_count");
        }

        public Image getEdit() {
            return this.edit;
        }

        public Image getDelete() {
            return this.delete;
        }
    }
}
