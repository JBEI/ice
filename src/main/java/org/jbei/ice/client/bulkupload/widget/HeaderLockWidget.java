package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */
public class HeaderLockWidget implements IsWidget {

    private final FocusPanel parent;
    private final HTMLPanel htmlPanel;
    private final Icon lockIcon;
    private final CellColumnHeader columnHeader;
    private final PopupHandler popUp;
    private final ServiceDelegate<PreferenceInfo> delegate;

    private TextBox defaultValueBox;

    public HeaderLockWidget(final CellColumnHeader columnHeader, ServiceDelegate<PreferenceInfo> lockUnlockDelegate) {
        if (columnHeader.isLocked()) {
            lockIcon = new Icon(FAIconType.LOCK);
            lockIcon.setTitle(columnHeader.getDefaultValue());
            lockIcon.addStyleName("bulk_upload_locked_header");
        } else {
            lockIcon = new Icon(FAIconType.UNLOCK_ALT);
            lockIcon.setTitle("Set " + columnHeader.getHeaderType().toString() + " default value");
        }

        lockIcon.addStyleName("display-inline");
        lockIcon.removeStyleName("font-awesome");
        lockIcon.addStyleName("opacity_hover");

        this.columnHeader = columnHeader;
        this.delegate = lockUnlockDelegate;

        HTMLPanel lockPanel = new HTMLPanel("<span id=\"creator_icon\"></span>");
        lockPanel.add(lockIcon, "creator_icon");
        lockPanel.setStyleName("display-inline");

        parent = new FocusPanel(lockPanel);
        parent.addStyleName("opacity_hover");

        htmlPanel = new HTMLPanel("<span id=\"creator_label\"></span><br><span id=\"creator_input\"></span><br><br>" +
                                          "<span id=\"lock_button\"></span>&nbsp;<span id=\"unlock_button\"></span>" +
                                          "&nbsp;<span id=\"cancel_action\"></span>");
        htmlPanel.setStyleName("bg_fc");
        htmlPanel.addStyleName("pad-6");
        popUp = new PopupHandler(this.htmlPanel, lockIcon.getElement(), false);
        parent.addClickHandler(popUp);
        createTableContents(columnHeader);
        this.asWidget().addStyleName("display-inline");
    }

    private void createTableContents(CellColumnHeader header) {
        addLabel(header.getHeaderType().toString() + " Default", "creator_label");
        defaultValueBox = new TextBox();
        defaultValueBox.setWidth("180px");
        defaultValueBox.setStyleName("input_box");
        defaultValueBox.setMaxLength(125);
        if (header.isLocked())
            defaultValueBox.setText(header.getDefaultValue());
        htmlPanel.add(defaultValueBox, "creator_input");

        Button lockSubmit = new Button("<i class=\"" + FAIconType.LOCK.getStyleName() + " font-11em\"></i> Lock");
        htmlPanel.add(lockSubmit, "lock_button");
        lockSubmit.addClickHandler(new LockClickHandler());

        Button unlockSubmit = new Button(
                "<i class=\"" + FAIconType.UNLOCK_ALT.getStyleName() + " font-11em\"></i> Unlock");
        htmlPanel.add(unlockSubmit, "unlock_button");
        unlockSubmit.addClickHandler(new UnlockClickHandler());

        HTML cancel = new HTML("<i class=\"" + FAIconType.REMOVE.getStyleName() + "\"></i> Close");
        cancel.setStyleName("edit_icon");
        cancel.addStyleName("font-85em");
        htmlPanel.add(cancel, "cancel_action");
        cancel.addClickHandler(new CancelClickHandler());
    }

    protected void addLabel(String label, String elementId) {
        String html = "<span class=\"font-70em\" style=\"white-space:nowrap\">" + label + "</span>";
        HTML widget = new HTML(html);
        widget.setStyleName("display-inline");
        htmlPanel.add(widget, elementId);
    }

    public String getCreator() {
        return this.defaultValueBox.getText().trim();
    }

    public void setCreator(String creator) {
        this.defaultValueBox.setText(creator);
    }

    /**
     * Returns the {@link com.google.gwt.user.client.ui.Widget} aspect of the receiver.
     */
    @Override
    public Widget asWidget() {
        return parent;
    }

    //
    // inner classes
    //
    private class LockClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            if (defaultValueBox.getText().isEmpty()) {
                defaultValueBox.setStyleName("input_box_error");
                popUp.show();
                lockIcon.setType(FAIconType.UNLOCK_ALT);
                lockIcon.removeStyleName("bulk_upload_locked_header");
                lockIcon.setTitle("Set " + columnHeader.getHeaderType().toString() + " default value");
                lockIcon.removeStyleName("font-awesome");
                columnHeader.setLocked(false);
                columnHeader.setDefaultValue("");
            } else {
                defaultValueBox.setStyleName("input_box");
                lockIcon.setType(FAIconType.LOCK);
                lockIcon.addStyleName("bulk_upload_locked_header");
                lockIcon.setTitle(columnHeader.getDefaultValue());
                columnHeader.setDefaultValue(defaultValueBox.getText());
                columnHeader.setLocked(true);
                lockIcon.removeStyleName("font-awesome");
                popUp.hidePopup();
                delegate.execute(new PreferenceInfo(true, columnHeader.toString().toUpperCase(),
                                                    columnHeader.getDefaultValue()));
            }
        }
    }

    private class UnlockClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            lockIcon.setType(FAIconType.UNLOCK_ALT);
            lockIcon.removeStyleName("bulk_upload_locked_header");
            lockIcon.setTitle("Set " + columnHeader.getHeaderType().toString() + " default value");
            lockIcon.removeStyleName("font-awesome");
            columnHeader.setLocked(false);
            columnHeader.setDefaultValue("");
            popUp.hidePopup();
            delegate.execute(new PreferenceInfo(false, columnHeader.toString().toUpperCase(),
                                                columnHeader.getDefaultValue()));
        }
    }

    private class CancelClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            defaultValueBox.setStyleName("input_box");
            popUp.hidePopup();
        }
    }
}
