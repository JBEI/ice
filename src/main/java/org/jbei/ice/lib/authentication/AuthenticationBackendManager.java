package org.jbei.ice.lib.authentication;

import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.ConfigurationKey;

/**
 * Manage Authentication Backends.
 * <p/>
 * Load the authentication backend as specified in the configuration file.
 *
 * @author Zinovii Dmytriv
 */
public class AuthenticationBackendManager {
    /**
     * Load the authentication backend specified in the configuration file.
     *
     * @return {@link IAuthenticationBackend} specified in the configuration file.
     * @throws AuthenticationBackendManagerException
     *
     */
    public static IAuthenticationBackend loadAuthenticationBackend()
            throws AuthenticationBackendManagerException {
        String backendName = Utils.getConfigValue(ConfigurationKey.AUTHENTICATION_BACKEND);

        try {
            final Class<?> authenticationBackendClass = Class.forName(backendName);

            return (IAuthenticationBackend) authenticationBackendClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new AuthenticationBackendManagerException(e);
        } catch (InstantiationException e) {
            throw new AuthenticationBackendManagerException(e);
        } catch (IllegalAccessException e) {
            throw new AuthenticationBackendManagerException(e);
        }
    }

    /**
     * Exception for {@link AuthenticationBackendManager}.
     *
     * @author Zinovii Dmytriv
     */
    public static class AuthenticationBackendManagerException extends Exception {
        private static final long serialVersionUID = -8208018784027094509L;

        public AuthenticationBackendManagerException(Throwable throwable) {
            super(throwable);
        }
    }
}
