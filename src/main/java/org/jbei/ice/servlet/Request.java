package org.jbei.ice.servlet;

/**
 * A shortcut to have gson parse the request instead of writing a separate parser to get the action
 *
 * @author Hector Plahar
 */
public class Request {

    private String action;
    private String entity;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return action + " " + entity;
    }
}
