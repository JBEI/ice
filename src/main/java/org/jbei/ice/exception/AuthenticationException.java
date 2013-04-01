package org.jbei.ice.exception;

import org.jbei.ice.controllers.common.ControllerException;

/**
 * @author Hector Plahar
 */
public class AuthenticationException extends Exception {

    /**
     * @inheritDoc
     */
    public AuthenticationException(String s, ControllerException e) {
        super(s, e);
    }
}
