package org.jbei.ice.lib.authentication;

import org.jbei.ice.lib.models.Account;

public abstract class AuthenticationBackend {
	public abstract Account authenticate(String userId, String password);
	
}
