package org.jbei.ice.servlet;

/**
 * Code constant values that are return on the server response object
 *
 * @author Hector Plahar
 */
public enum CodeConstant {

    // general constants
    SERVER_ERROR("000"),
    REQUEST_INVALID("001"),
    SESSION_INVALID("010"),
    UNAUTHORIZED_ACCESS("011");

    private String code;

    CodeConstant(String code) {
        this.code = code;
    }

    public static String getCode(Entity entity, EntityAction entityAction, Outcome outcome) {
        StringBuilder builder = new StringBuilder();
        builder.append(entity.getCode()).append(entityAction.getCode()).append(outcome.getCode());
        return builder.toString();
    }

    @Override
    public String toString() {
        return this.code;
    }

    public String getCode() {
        return this.code;
    }
}
