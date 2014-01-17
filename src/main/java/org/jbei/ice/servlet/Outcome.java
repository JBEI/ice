package org.jbei.ice.servlet;

/**
 * @author Hector Plahar
 */
public enum Outcome {

    SUCCESS('0'),
    FAILURE('1'),
    INVALID('2'),
    SERVER_ERROR('3'),
    INSUFFICIENT_PERMISSIONS('4');

    public char code;

    Outcome(char c) {
        this.code = c;
    }

    public char getCode() {
        return this.code;
    }
}
