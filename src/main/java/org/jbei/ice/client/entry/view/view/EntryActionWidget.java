package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Edit / Delete widget for entries
 *
 * @author Hector Plahar
 */
public class EntryActionWidget extends Composite {

    private HTML edit;
    private HTML delete;
    private HandlerRegistration deleteRegistration;
    private HandlerRegistration editRegistration;

    public EntryActionWidget() {
        initComponents();
        String html = "<span id=\"edit_general_label\"></span><span style=\"color: #eee\">&nbsp;|&nbsp;</span>"
                + "<span id=\"delete_general_label\"></span>";
        HTMLPanel panel = new HTMLPanel(html);
        initWidget(panel);
        panel.add(edit, "edit_general_label");
        panel.add(delete, "delete_general_label");
    }

    private void initComponents() {
        edit = new HTML("<i class=\"" + FAIconType.EDIT.getStyleName() + " font-80em\"></i>");
        edit.setStyleName("edit_icon");
        edit.setTitle("Edit");

        delete = new HTML("<i class=\"" + FAIconType.TRASH.getStyleName() + " font-80em\"></i>");
        delete.setStyleName("delete_icon");
        delete.setTitle("Delete");
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
}
