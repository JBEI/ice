package org.jbei.ice.lib.permissions;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.ProtectedPage;
import org.jbei.ice.web.pages.WelcomePage;

/**
 * Authorization Strategy for gd-ice.
 * <p>
 * Overrides {@link #isInstantiationAuthorized(Class)} method to protect pages.
 * 
 * @see #isActionAuthorized(Component, Action)
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class IceAuthorizationStrategy implements IAuthorizationStrategy,
        IUnauthorizedComponentInstantiationListener {

    @Override
    public boolean isActionAuthorized(Component component, Action action) {
        return true;
    }

    /**
     * Force the session user to be authenticated before {@link ProtectedPage}s are instantiated.
     */
    @Override
    public <T extends Component> boolean isInstantiationAuthorized(Class<T> componentClass) {
        if (ProtectedPage.class.isAssignableFrom(componentClass)) {
            return IceSession.get().isAuthenticated();
        }
        return true;
    }

    /**
     * Display the {@link WelcomePage} if Component instantiation is not authorized.
     * <p>
     * 
     * @see #isActionAuthorized(Component, Action)
     */
    @Override
    public void onUnauthorizedInstantiation(Component component) {
        throw new RestartResponseAtInterceptPageException(WelcomePage.class);
    }
}
