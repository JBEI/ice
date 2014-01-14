package org.jbei.ice.lib.account;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * Type of account managed by the system
 *
 * @author Hector Plahar
 */
public enum AccountType implements IDataTransferModel {

    @Deprecated
    SYSTEM("System"),

    ADMIN("Administrator"),
    NORMAL("Regular");

    private String display;

    AccountType() {
    }

    AccountType(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return this.display;
    }
}
