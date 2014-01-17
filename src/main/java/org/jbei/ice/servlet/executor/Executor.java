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

    public abstract Result execute();
}
