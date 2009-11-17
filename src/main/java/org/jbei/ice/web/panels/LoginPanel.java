package org.jbei.ice.web.panels;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.LogOutPage;
import org.jbei.ice.web.pages.LoginPage;
import org.jbei.ice.web.pages.PlasmidPage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.pages.RegisterPage;

/**
 * @author tham
 */
public class LoginPanel extends Panel {
	private Fragment preLoginFragment, postLoginFragment;
	
	public LoginPanel(String id) {
		super(id);
		preLoginFragment = createPreLoginFragment();
		add(preLoginFragment);
		postLoginFragment = createPostLoginFragment();
		add(postLoginFragment);
	}
	
	private String getEmail() {
		String result = null;
		IceSession s = IceSession.get();
		if (s.isAuthenticated()) {
			result = s.getAccount().getEmail();
		} else {
			result = "";
		}
		return result;
	}
	
	private Fragment createPreLoginFragment () {
		Fragment preLogin = new Fragment("preLoginPanel", "preLogin", this) {
			
			public boolean isVisible() {
				IceSession s = IceSession.get();
				return !s.isAuthenticated();
				
			}
		};
		
		preLogin.add(new BookmarkablePageLink("logIn", LoginPage.class));
		preLogin.add(new BookmarkablePageLink("register", RegisterPage.class));
		return preLogin;
	}
	
	private Fragment createPostLoginFragment() {
		Fragment postLogin = new Fragment("postLoginPanel", "postLogin", LoginPanel.this) {
			
			public boolean isVisible() {
				IceSession s = IceSession.get();
				return s.isAuthenticated();
			}
		};
		postLogin.add(new BookmarkablePageLink("userProfile", PlasmidPage.class)
			.add(new Label("userName", getEmail())));
		postLogin.add(new BookmarkablePageLink("logOut", LogOutPage.class));
		

		return postLogin;
	}
}

