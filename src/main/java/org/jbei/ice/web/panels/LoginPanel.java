package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.LogOutPage;
import org.jbei.ice.web.ProfilePage;

/**
 * @author tham
 */
public class LoginPanel extends Panel {
	private Fragment currentFragment, otherFragment;
	
	public LoginPanel(String id) {
		super(id);
		System.out.println(""+id);
		currentFragment = createPreLoginFragment();
		add(currentFragment);
		
	}
	
	private Fragment createPreLoginFragment () {
		Fragment preLogin = new Fragment("loginPanel", "preLogin", this);
		//preLogin.add(new BookmarkablePageLink("logIn", LoginPage.class));
		//preLogin.add(new BookmarkablePageLink("register", RegisterPage.class));
		return preLogin;
	}
	
	private Fragment createPostLoginFragment() {
		Fragment postLogin = new Fragment("loginPanel", "postLogin", this);
		postLogin.add(new BookmarkablePageLink("userProfile", ProfilePage.class));
		postLogin.add(new BookmarkablePageLink("logOut", LogOutPage.class));
		
		
		return postLogin;
	}
}

