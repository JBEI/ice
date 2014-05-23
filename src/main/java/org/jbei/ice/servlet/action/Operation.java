package org.jbei.ice.servlet.action;

/**
 * Operations that the various executors are asked to perform in response to user requests
 *
 * @author Hector Plahar
 */
public enum Operation {

    CREATE,
    RETRIEVE,
    UPDATE,
    DELETE;

    public static Operation fromString(String operationStr) {
        for (Operation operation : Operation.values()) {
            if (operationStr.equalsIgnoreCase(operation.name()))
                return operation;
        }
        return null;
    }
}
