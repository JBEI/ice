package org.jbei.ice.lib.shared.dto;

/**
 * Type of account managed by the system
 *
 * @author Hector Plahar
 */
public enum AccountType implements IDTOModel {
    ADMIN("Administrator"), NORMAL("Regular"), SYSTEM("System");

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
