package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.LogOutPage;
import org.jbei.ice.web.pages.RegistrationPage;
import org.jbei.ice.web.pages.UserEntryPage;
import org.jbei.ice.web.pages.WelcomePage;

/**
 * @author tham
 */
public class LoginStatusPanel extends Panel {
	private static final long serialVersionUID = 1L;
	private Fragment preLoginFragment, postLoginFragment;
	
	public LoginStatusPanel(String id) {
		super(id);
		preLoginFragment = createPreLoginFragment();
		add(preLoginFragment);
		postLoginFragment = createPostLoginFragment();
		add(postLoginFragment);
	}
	
	private String getEmail() {
		String result = null;
		IceSession s = (IceSession) getSession();
		if (s.isAuthenticated()) {
			result = s.getAccount().getEmail();
		} else {
			result = "";
		}
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	private Fragment createPreLoginFragment () {
		Fragment preLogin = new Fragment("preLoginPanel", "preLogin", this) {
			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				IceSession s = (IceSession) getSession();
				return !s.isAuthenticated();
				
			}
		};
		
		preLogin.add(new BookmarkablePageLink("logIn", WelcomePage.class));
		
		preLogin.add(new BookmarkablePageLink("register", RegistrationPage.class));
		return preLogin;
	}
	
	@SuppressWarnings("unchecked")
	private Fragment createPostLoginFragment() {
		Fragment postLogin = new Fragment("postLoginPanel", "postLogin", LoginStatusPanel.this) {
			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				IceSession s = (IceSession) getSession();
				return s.isAuthenticated();
			}
		};
		postLogin.add(new BookmarkablePageLink("userProfile", UserEntryPage.class)
			.add(new Label("userName", getEmail())));
		postLogin.add(new BookmarkablePageLink("logOut", LogOutPage.class));
		
		return postLogin;
	}
}

