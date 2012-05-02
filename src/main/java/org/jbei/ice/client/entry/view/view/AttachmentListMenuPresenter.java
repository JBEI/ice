package org.jbei.ice.client.entry.view.view;

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
    }

    private final IAttachmentListMenuView view;
    private HandlerRegistration quickAddHandler;

    public AttachmentListMenuPresenter(IAttachmentListMenuView view) {
        this.view = view;
        addClickHandlers();
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

    /**
     * @param item
     *            attachment item in the cell being clicked on
     * @return Clickhandler for each cell in the attachment list menu
     *         to download
     */
    public ClickHandler getCellClickHandler(final AttachmentItem item) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Window.Location.replace("/download?type=attachment&name=" + item.getName() + "&id="
                        + item.getFileId());
            }
        };
    }
}
