package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;

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
        @Source("org/jbei/ice/client/resource/image/plus.png")
        ImageResource plusImage();

        @Source("org/jbei/ice/client/resource/image/minus.png")
        ImageResource minusImage();

        @Source("org/jbei/ice/client/resource/image/edit.png")
        ImageResource editImage();

        @Source("org/jbei/ice/client/resource/image/delete.png")
        ImageResource deleteImage();
    }

    private final FlexTable table;
    private long currentSelected;
    private final TextBox quickAddBox;
    private final Image quickAddButton;
    private int row;
    private final Resources resources = GWT.create(Resources.class);

    public CollectionUserMenu() {

        table = new FlexTable();
        initWidget(table);

        // quick add widgets
        quickAddBox = new TextBox();
        quickAddBox.setStyleName("input_box");
        quickAddBox.setText("Enter new collection name...");
        quickAddBox.setWidth("99%");
        quickAddButton = new Image(resources.plusImage());
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

    public void setFolderDetails(ArrayList<FolderDetails> folders) {
        if (folders == null || folders.isEmpty())
            return;

        for (FolderDetails folder : folders) {
            addFolderDetail(folder);
        }
    }

    public boolean isValidClick(ClickEvent event) {
        if (event == null)
            return false;

        Cell cell = this.table.getCellForEvent(event);
        if (cell == null)
            return false;

        boolean isValid = (cell.getCellIndex() != 0 || cell.getRowIndex() != 0);
        if (!isValid)
            return isValid;

        return isValid;
    }

    public void addFolderDetail(FolderDetails folder) {
        if (folder == null)
            return;

        final MenuCell cell = new MenuCell(folder);
        cell.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                currentSelected = cell.getFolderId();
            }
        });

        row += 1;
        table.setWidget(row, 0, cell);
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

    public long getCurrentSelection() {
        return currentSelected;
    }

    public void switchButton() {
        if (quickAddBox.isVisible()) {
            quickAddButton.setUrl(resources.plusImage().getSafeUri());
            quickAddButton.setStyleName("collection_quick_add_image");
            quickAddBox.setVisible(false);
            quickAddBox.setStyleName("input_box");
        } else {
            quickAddButton.setUrl(resources.minusImage().getSafeUri());
            quickAddButton.setStyleName("collection_quick_add_image");
            quickAddBox.setText("");
            quickAddBox.setVisible(true);
        }
    }

    public void hideQuickText() {
        quickAddButton.setUrl(resources.plusImage().getSafeUri());
        quickAddButton.setStyleName("collection_quick_add_image");
        quickAddBox.setVisible(false);
        quickAddBox.setStyleName("input_box");
    }

    // inner class
    private class MenuCell extends Composite implements HasClickHandlers {

        private final HTMLPanel panel;
        private final FolderDetails folder;
        private final String html;

        private final Label count;
        private final HoverCell action;

        public MenuCell(final FolderDetails folder) {

            super.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);

            this.folder = folder;
            action = new HoverCell();
            action.getEdit().addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    // TODO : probably should be moved outside of this class
                    event.stopPropagation();
                    Cell cell = table.getCellForEvent(event);
                    TextBox box = new TextBox();
                    box.setText(folder.getName());
                    table.setWidget(cell.getRowIndex(), cell.getCellIndex(), box);
                    box.setFocus(true);
                }
            });

            html = "<span style=\"padding: 5px\" class=\"collection_user_menu\">"
                    + folder.getName() + "</span><span id=\"right" + folder.getId() + "\"></span>";

            panel = new HTMLPanel(html);

            count = new Label(formatNumber(folder.getCount()));
            count.setStyleName("menu_count");

            panel.add(count, "right" + folder.getId());
            panel.setStyleName("collection_user_menu_row");
            initWidget(panel);
        }

        public long getFolderId() {
            return this.folder.getId();
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);

            switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEOVER:
                panel.remove(count);
                panel.add(action, "right" + folder.getId());
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

                panel.remove(action);
                panel.add(count, ("right" + folder.getId()));
                break;
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

            edit = new Image(resources.editImage());
            delete = new Image(resources.deleteImage());

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
