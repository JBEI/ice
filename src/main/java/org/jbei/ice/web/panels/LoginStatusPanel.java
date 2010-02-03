package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.LogOutPage;
import org.jbei.ice.web.pages.LoginPage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.pages.RegistrationPage;

public class LoginStatusPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Fragment preLoginFragment, postLoginFragment;

    private Account account = null;

    public LoginStatusPanel(String id) {
        super(id);

        account = IceSession.get().getAccount();

        preLoginFragment = createPreLoginFragment();
        add(preLoginFragment);

        postLoginFragment = createPostLoginFragment();
        add(postLoginFragment);
    }

    @SuppressWarnings("unchecked")
    private Fragment createPreLoginFragment() {
        Fragment preLogin = new Fragment("preLoginPanel", "preLogin", this) {
            private static final long serialVersionUID = 1L;

            public boolean isVisible() {
                return !IceSession.get().isAuthenticated();
            }
        };

        preLogin.add(new BookmarkablePageLink("logIn", LoginPage.class));
        preLogin.add(new BookmarkablePageLink("register", RegistrationPage.class));

        return preLogin;
    }

    @SuppressWarnings("unchecked")
    private Fragment createPostLoginFragment() {
        Fragment postLogin = new Fragment("postLoginPanel", "postLogin", LoginStatusPanel.this) {
            private static final long serialVersionUID = 1L;

            public boolean isVisible() {
                return IceSession.get().isAuthenticated();
            }
        };

        postLogin.add(new BookmarkablePageLink("userProfile", ProfilePage.class,
                new PageParameters("0=about,1=" + ((account != null) ? account.getEmail() : "")))
                .add(new Label("userName", IceSession.get().isAuthenticated() ? account
                        .getFullName() : "")));

        postLogin.add(new BookmarkablePageLink("logOut", LogOutPage.class));

        return postLogin;
    }
}