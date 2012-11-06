package org.jbei.ice.client.common.footer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class FeedbackWidget extends Composite {

    private final FlexTable layout;
    private final TextBox emailBox;
    private final TextArea messageArea;
    private final Button submit;
    private final Button close;

    public FeedbackWidget() {
        layout = new FlexTable();
        layout.setWidth("340px");
        layout.setStyleName("font-80em");
        layout.addStyleName("bg_f8");
        layout.addStyleName("pad-3");
        initWidget(layout);

        emailBox = new TextBox();
        emailBox.setStyleName("footer_email_input");
        messageArea = new TextArea();
        messageArea.setSize("240px", "130px");
        messageArea.setStyleName("footer_email_input");

        submit = new Button("Submit");
        close = new Button("Close");
        close.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER)
                    return;

                close.click();
            }
        });

        addWidgets();
    }

    public void clear() {
        this.emailBox.setText("");
        this.messageArea.setText("");
    }

    public String getEmail() {
        return this.emailBox.getText();
    }

    public String getMessage() {
        return this.messageArea.getText();
    }

    private void addWidgets() {
        layout.setHTML(0, 0, "<b>Email</b> <span class=\"required\">*</span>");
        layout.setWidget(0, 1, emailBox);

        layout.setHTML(1, 0, "<b>Message</b> <span class=\"required\">*</span>");
        layout.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        layout.setWidget(1, 1, messageArea);
        layout.setWidget(2, 0, close);
        layout.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasAlignment.ALIGN_RIGHT);
        layout.setWidget(2, 1, submit);
        layout.getFlexCellFormatter().setHorizontalAlignment(2, 1, HasAlignment.ALIGN_LEFT);
    }

    public void addCloseHandler(ClickHandler handler) {
        close.addClickHandler(handler);
    }

    public void addSubmitHandler(final ClickHandler handler) {

        submit.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                boolean hasError = false;
                if (emailBox.getText().trim().isEmpty()) {
                    emailBox.setStyleName("footer_email_input_error");
                    hasError = true;
                } else {
                    emailBox.setStyleName("footer_email_input");
                }

                if (messageArea.getText().trim().isEmpty()) {
                    messageArea.setStyleName("footer_email_input_error");
                    hasError = true;
                } else {
                    messageArea.setStyleName("footer_email_input");
                }

                if (hasError)
                    return;

                handler.onClick(event);
            }
        });
    }
}
