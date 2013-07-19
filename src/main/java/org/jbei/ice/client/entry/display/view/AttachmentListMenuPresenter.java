package org.jbei.ice.client.entry.display.view;

import java.util.ArrayList;

import org.jbei.ice.client.entry.display.handler.HasAttachmentDeleteHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;

/**
 * Presenter for the attachment list menu
 *
 * @author Hector Plahar
 */
public class AttachmentListMenuPresenter {

    public interface IAttachmentListMenuView {

        void switchAttachmentAddButton();

        HandlerRegistration addQuickAddHandler(ClickHandler handler);

        void addMenuItem(AttachmentItem item, int itemCount);
    }

    private final IAttachmentListMenuView view;
    private HandlerRegistration quickAddHandler;
    private int itemCount;
    private final ArrayList<AttachmentItem> list;

    public AttachmentListMenuPresenter(IAttachmentListMenuView view) {
        this.view = view;
        addClickHandlers();
        list = new ArrayList<AttachmentItem>();
    }

    public ArrayList<AttachmentItem> getAttachmentItems() {
        return list;
    }

    protected void addClickHandlers() {
        if (quickAddHandler != null)
            quickAddHandler.removeHandler();

        quickAddHandler = this.view.addQuickAddHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                view.switchAttachmentAddButton();
            }
        });
    }

    public void addAttachmentItem(AttachmentItem item) {
        if (item == null)
            return;

        list.add(item);
        view.addMenuItem(item, itemCount);
        itemCount += 1;
    }

    public void reset() {
        list.clear();
        itemCount = 0;
    }

    /**
     * @param item attachment item in the cell being clicked on
     * @return Clickhandler for each cell in the attachment list menu
     *         to download
     */
    public ClickHandler getCellClickHandler(final AttachmentItem item) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Window.Location.replace("/download?type=attachment&name=" + item.getName() + "&id=" + item.getFileId());
            }
        };
    }

    public ClickHandler getDeleteClickHandler(final HasAttachmentDeleteHandler handler, final AttachmentItem item) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm("This action cannot be undone.\n\nContinue?"))
                    handler.deleteAttachment(item);
            }
        };
    }
}
