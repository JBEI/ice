package org.jbei.ice.services.rest;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class ErrorResponse implements IDataTransferModel {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
