package org.jbei.ice.web.common;

import org.jbei.ice.lib.logging.Logger;

public class FormException extends Exception {
    private static final long serialVersionUID = -5055229029852511835L;

    public FormException(String string) {
        super(string);

        Logger.error(string, new Exception("Error"));
    }
}
