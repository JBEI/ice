package org.jbei.ice.lib.permissions;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.LoginPage;
import org.jbei.ice.web.pages.ProtectedPage;

public class IceAuthorizationStrategy implements IAuthorizationStrategy,
		IUnauthorizedComponentInstantiationListener {

	public boolean isActionAuthorized(Component component, Action action) {
		return true;
	}

	public <T extends Component> boolean isInstantiationAuthorized(Class<T> componentClass) {
		if (ProtectedPage.class.isAssignableFrom(componentClass)) {
			return IceSession.get().isAuthenticated();
		}

		return true;
	}

	public void onUnauthorizedInstantiation(Component component) {
		throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}
}
