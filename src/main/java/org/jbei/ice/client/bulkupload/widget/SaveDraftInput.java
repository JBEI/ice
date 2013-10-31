package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SaveDraftInput extends Composite {

    private final TextBox inputName;
    private final Button cancel;

    public SaveDraftInput() {
        inputName = new TextBox();
        inputName.setMaxLength(36);
        inputName.getElement().setAttribute("placeholder", "Enter draft name");
        inputName.setStyleName("saved_draft_input");
        cancel = new Button("<i class=\"" + FAIconType.TIMES.getStyleName() + "\"></i>");
        cancel.addStyleName("remove_filter");

        String html = "<span id=\"save_draft_inputbox\"></span>&nbsp;<span id=\"cancel_rename\"></span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.setStyleName("display-inline");
        initWidget(panel);

        panel.add(inputName, "save_draft_inputbox");
        panel.add(cancel, "cancel_rename");
    }

    public String getDraftName() {
        return inputName.getText();
    }

    public void setRename(String name) {
        this.inputName.setText(name);
        this.inputName.setFocus(true);
        this.inputName.selectAll();
    }

    public void setCancelHandler(ClickHandler handler) {
        this.cancel.addClickHandler(handler);
    }

    public void setKeyPressHandler(KeyPressHandler handler) {
        inputName.addKeyPressHandler(handler);
    }

    public void reset() {
        inputName.setText("");
    }
}
