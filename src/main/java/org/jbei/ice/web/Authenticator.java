package org.jbei.ice.web;

import org.jbei.ice.lib.models.Account;

public abstract class Authenticator {
	public abstract Account authenticate(String userId, String password);
	
}
