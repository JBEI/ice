package org.jbei.ice.client.login;

import java.util.ArrayList;

import org.jbei.ice.client.FeedbackType;
import org.jbei.ice.client.IFeedbackHandler;
import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderView;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View of the login page
 * 
 * @author Hector Plahar
 */
public class LoginView extends Composite implements LoginPresenter.Display {

    private final TextBox login;
    private final TextBox pass;
    private final Button loginButton;
    private final CheckBox rememberUserCheckBox;
    private final HeaderView header;
    private final FeedbackLabel feedback;
    private FlexTable loginTable;

    public LoginView() {

        FlexTable main = new FlexTable();
        main.setCellPadding(0);
        main.setCellSpacing(0);
        main.setWidth("100%");
        main.setHeight("98%");
        initWidget(main);

        // header
        header = new HeaderView();
        main.setWidget(0, 0, header);

        // initialize components for login box
        login = new TextBox();
        login.setStyleName("inputbox");
        login.setWidth("200px");

        pass = new PasswordTextBox();
        pass.setStyleName("inputbox");
        pass.setWidth("200px");

        loginButton = new Button("Log In");
        rememberUserCheckBox = new CheckBox("Remember me on this computer");

        // feedback
        feedback = new FeedbackLabel();

        // add login to page
        main.setWidget(1, 0, this.createLogin());
        main.getCellFormatter().setHeight(1, 0, "100%");
        main.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);

        // footer
        main.setWidget(2, 0, Footer.getInstance());
    }

    protected Widget createLogin() {

        FlexTable wrapper = new FlexTable();
        wrapper.setCellPadding(10);
        wrapper.setWidth("100%");

        // left
        wrapper.setHTML(0, 0, "&nbsp;");
        wrapper.getCellFormatter().setHeight(0, 0, "100%");

        // right cell contents
        //        FormPanel form = new FormPanel();
        loginTable = new FlexTable();
        loginTable.addStyleName("data_table");
        loginTable.setWidth("100%");
        loginTable.setCellPadding(3);
        loginTable.setCellSpacing(1);

        loginTable.setHTML(0, 0, "<span class=\"panel_header\">Login</span>");
        loginTable.getCellFormatter().addStyleName(0, 0, "title_row_header");

        loginTable.setWidget(1, 0, loginContents());
        loginTable.getCellFormatter().addStyleName(1, 0, "background_white");

        // format right cell
        //        form.setWidget(loginTable);
        wrapper.setWidget(0, 1, loginTable);
        wrapper.getCellFormatter().setWidth(0, 1, "380px");
        wrapper.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);

        //        form.addSubmitHandler(new SubmitHandler() {
        //
        //            @Override
        //            public void onSubmit(SubmitEvent event) {
        //                Window.alert("form submitted");
        //
        //            }
        //        });
        return wrapper;
    }

    protected Widget loginContents() {

        FlexTable contents = new FlexTable();
        contents.setCellPadding(0);
        contents.setCellSpacing(2);

        // feedback Panel
        contents.setWidget(0, 0, feedback);
        contents.getFlexCellFormatter().setColSpan(0, 0, 2);

        contents.setHTML(1, 0, "Login: <span class=\"required\">*</span>");
        contents.getCellFormatter().setWidth(1, 0, "150px");
        contents.setWidget(1, 1, login);

        // password
        contents.setHTML(2, 0, "Password: <span class=\"required\">*</span>");
        contents.setWidget(2, 1, pass);

        // cookies
        contents.setHTML(3, 0, "&nbsp;");
        contents.setWidget(3, 1, rememberUserCheckBox);

        // login button
        contents.setHTML(4, 0, "&nbsp;");
        contents.setWidget(4, 1, loginButton);

        contents.setHTML(5, 0, "&nbsp;");
        contents.setHTML(5, 1, "&nbsp;");

        contents.setHTML(6, 0, "&nbsp;");
        contents.setHTML(6, 1, "&nbsp;");

        return contents;
    }

    @Override
    public HasClickHandlers getLoginButton() {
        return loginButton;
    }

    @Override
    public String getLoginName() {
        return this.login.getText();
    }

    @Override
    public String getLoginPass() {
        return this.pass.getText();
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public boolean rememberUserOnComputer() {
        return rememberUserCheckBox.getValue();
    }

    @Override
    public ILogoutHandler getLogoutHandler() {
        return header;
    }

    @Override
    public void setFeedback(ArrayList<String> msgs, FeedbackType type) {
        feedback.setText(msgs, type);
    }

    private class FeedbackLabel extends HTML implements IFeedbackHandler {

        @Override
        public void setText(ArrayList<String> msgs, FeedbackType type) {

            switch (type) {
            case ERROR:
                this.setStyleName("feedback_panel_err");
                break;

            case FEEDBACK:
                break;
            }

            String html = "<ul>";
            for (String msg : msgs) {
                html += "<li>" + msg + "</li>";
            }
            html += "</ul>";
            setHTML(html);
        }
    }

    @Override
    public void setKeyPressHandler(KeyPressHandler handler) {
        this.login.addKeyPressHandler(handler);
        this.pass.addKeyPressHandler(handler);
    }
}
