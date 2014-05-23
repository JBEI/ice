package org.jbei.ice.servlet;

/**
 * @author Hector Plahar
 */
public enum EntityAction {

    CREATE('0'),
    RETRIEVE('1'),
    UPDATE('2'),
    DELETE('3'),
    LOGIN('4'),
    LOGOUT('5');

    private char code;

    EntityAction(char code) {
        this.code = code;
    }

    public char getCode() {
        return this.code;
    }
}
