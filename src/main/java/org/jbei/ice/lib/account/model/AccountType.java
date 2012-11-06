package org.jbei.ice.lib.account.model;

/**
 * Type of account
 *
 * @author Hector Plahar
 */
public enum AccountType {
    ADMIN("Administrator"), NORMAL("Regular");

    private final String display;

    AccountType(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return this.display;
    }
}
