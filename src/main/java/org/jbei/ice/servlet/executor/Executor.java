package org.jbei.ice.servlet.executor;

import org.jbei.ice.servlet.Result;
import org.jbei.ice.servlet.action.Action;
import org.jbei.ice.servlet.action.Operation;

/**
 * Parent class action execution.
 *
 * @author Hector Plahar
 */
public abstract class Executor<T extends Action> {

    protected final T action;

    public Executor(T action) {
        this.action = action;
    }

    protected Operation getOperation() {
        return Operation.fromString(action.getAction());
    }

    public Result execute() {

        switch (getOperation()) {
            case CREATE:
                return create();

            case RETRIEVE:
                return retrieve();

            case UPDATE:
                return update();

            case DELETE:
                return delete();

            default:
                return executeOther();
        }
    }

    protected abstract Result create();

    protected abstract Result retrieve();

    protected abstract Result update();

    protected abstract Result delete();

    protected abstract Result executeOther();
}
