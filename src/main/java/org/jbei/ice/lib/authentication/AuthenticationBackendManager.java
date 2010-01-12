package org.jbei.ice.lib.authentication;

import org.jbei.ice.lib.utils.JbeirSettings;

public class AuthenticationBackendManager {
	public static IAuthenticationBackend loadAuthenticationBackend()
			throws AuthenticationBackendManagerException {
		String backendName = JbeirSettings.getSetting("AUTHENTICATION_BACKEND");

		try {
			final Class<?> authenticationBackendClass = Class
					.forName(backendName);

			return (IAuthenticationBackend) authenticationBackendClass
					.newInstance();
		} catch (ClassNotFoundException e) {
			throw new AuthenticationBackendManagerException(e);
		} catch (InstantiationException e) {
			throw new AuthenticationBackendManagerException(e);
		} catch (IllegalAccessException e) {
			throw new AuthenticationBackendManagerException(e);
		}
	}

	public static class AuthenticationBackendManagerException extends Exception {
		private static final long serialVersionUID = -8208018784027094509L;

		public AuthenticationBackendManagerException(Throwable throwable) {
			super(throwable);
		}
	}
}
