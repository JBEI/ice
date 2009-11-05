package org.jbei.ice.lib.managers;

@SuppressWarnings("serial")
public class ManagerException extends Exception {
	public ManagerException() {
	}

	public ManagerException(String message) {
		super(message);
	}
	
	public ManagerException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
