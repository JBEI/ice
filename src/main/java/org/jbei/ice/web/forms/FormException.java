package org.jbei.ice.web.forms;

import org.jbei.ice.lib.logging.Logger;

public class FormException extends Exception {

	public FormException(String string) {
		
		super(string);
		Logger.error(string);
	}

}
