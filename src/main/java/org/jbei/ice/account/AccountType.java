package org.jbei.ice.account;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Type of account managed by the system
 *
 * @author Hector Plahar
 */
public enum AccountType implements IDataTransferModel {

    ADMIN("Administrator"),
    PRINCIPAL_INVESTIGATOR("Principal Investigator"),
    NORMAL("Regular");

    private final String display;

    AccountType(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return this.display;
    }
}
