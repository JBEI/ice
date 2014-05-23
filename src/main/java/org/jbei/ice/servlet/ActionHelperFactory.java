package org.jbei.ice.servlet;

import org.jbei.ice.servlet.action.Action;
import org.jbei.ice.servlet.action.PartAction;

import com.google.gson.Gson;

/**
 * Factory class for mapping entities to actions
 *
 * @author Hector Plahar
 */
public class ActionHelperFactory {

    public static Action getAction(Gson gson, Request request, String json) {
        if (request == null)
            return null;

        switch (request.getEntity().toLowerCase().trim()) {
            case "part":
                return gson.fromJson(json, PartAction.class);

            default:
                return null;
        }
    }
}
