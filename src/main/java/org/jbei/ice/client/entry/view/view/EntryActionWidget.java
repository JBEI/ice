package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * Edit / Delete widget for entries
 *
 * @author Hector Plahar
 */
public class EntryActionWidget extends Composite {

    private HTML edit;
    private HTML pipe1;
    private HTML delete;
    private HTML pipe2;
    private HTML flag;
    private HandlerRegistration deleteRegistration;
    private HandlerRegistration editRegistration;
    private FlexTable layout;

    public EntryActionWidget() {
        initComponents();
        layout.setWidget(0, 0, edit);
        layout.getFlexCellFormatter().setStyleName(0, 0, "pad-left-40");
        layout.setWidget(0, 1, pipe1);
        layout.setWidget(0, 2, delete);
        layout.setWidget(0, 3, pipe2);
        layout.setWidget(0, 4, flag);
        initWidget(layout);
    }

    private void initComponents() {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(5);

        String html = "<i class=\"" + FAIconType.EDIT.getStyleName() + "\"></i> <span class=\"font-80em\">Edit</span>";
        edit = new HTML(html);
        edit.setStyleName("edit_icon");
        edit.setTitle("Edit Entry");

        pipe1 = new HTML("<span style=\"color: #ccc\">&nbsp;|&nbsp;</span>");

        delete = new HTML(
                "<i class=\"" + FAIconType.TRASH.getStyleName() + "\"></i> <span class=\"font-80em\">Delete</span>");
        delete.setStyleName("delete_icon");
        delete.setTitle("Delete");
        pipe2 = new HTML("<span style=\"color: #ccc\">&nbsp;|&nbsp;</span>");
        flag = new HTML("<i class=\"" + FAIconType.FLAG_CHECKERED.getStyleName()
                                + "\"></i> <span class=\"font-80em\">Flag</span> <i class=\"font-70em "
                                + FAIconType.CARET_DOWN.getStyleName() + "\"></i>");
        flag.setStyleName("flag_icon");
    }

    public void addDeleteEntryHandler(ClickHandler handler) {
        if (deleteRegistration != null)
            deleteRegistration.removeHandler();

        deleteRegistration = delete.addClickHandler(handler);
    }

    public void addEditButtonHandler(ClickHandler handler) {
        if (editRegistration != null)
            editRegistration.removeHandler();
        editRegistration = edit.addClickHandler(handler);
    }

    @Override
    public void setVisible(boolean visible) {
        edit.setVisible(visible);
        pipe1.setVisible(visible);
        delete.setVisible(visible);
        pipe2.setVisible(visible);
    }
}
