package org.jbei.ice.lib.permissions;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.ProtectedPage;
import org.jbei.ice.web.pages.WelcomePage;

public class IceAuthorizationStrategy implements IAuthorizationStrategy, 
	IUnauthorizedComponentInstantiationListener {

	@Override
	public boolean isActionAuthorized(Component component, Action action) {
		//TODO 
		return true;
	}

	@Override
	public <T extends Component> boolean isInstantiationAuthorized(
			Class<T> componentClass) {
		if (ProtectedPage.class.isAssignableFrom(componentClass)) {
			return IceSession.get().isAuthenticated();
		}
		return true;
	}
	
	@Override
	public void onUnauthorizedInstantiation(Component component) {
		  throw new RestartResponseAtInterceptPageException(
		      WelcomePage.class);
		}

}
