package org.jbei.ice.servlet;

/**
 * @author Hector Plahar
 */
public enum Entity {

    ACCOUNT('1'),
    ACTIVITY('2'),
    ASSEMBLY('3'),
    BIN('4'),
    CONSTRUCT('5'),
    DESIGN('6'),
    HISTORY('7'),
    PROJECT('8'),
    REQUEST('9');

    private char prefix;

    Entity(char codePrefix) {
        this.prefix = codePrefix;
    }

    public char getCode() {
        return this.prefix;
    }
}
