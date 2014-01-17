package org.jbei.ice.servlet;

import org.jbei.ice.servlet.action.Action;

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

        switch (request.getEntity().toLowerCase()) {
//            case "project":
//                return gson.fromJson(json, ProjectAction.class);
//
//            case "account":
//                return gson.fromJson(json, AccountAction.class);
//
//            case "request":
//                return gson.fromJson(json, RequestAction.class);
//
//            case "design":
//                return gson.fromJson(json, DesignAction.class);
//
//            case "activity":
//                return gson.fromJson(json, ActivityAction.class);
//
//            case "assembly":
//                return gson.fromJson(json, AssemblyAction.class);

            default:
                return null;
        }
    }
}
