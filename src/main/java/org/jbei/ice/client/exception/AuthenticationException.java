package org.jbei.ice.client.exception;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AuthenticationException extends Exception implements IsSerializable {

    private static final long serialVersionUID = 1L;

    public AuthenticationException() {
        super();
    }

    /**
     * @inheritDoc
     */
    public AuthenticationException(String s, Exception e) {
        super(s, e);
    }

    /**
     * @inheritDoc
     */
    public AuthenticationException(String s) {
        super(s);
    }
}
