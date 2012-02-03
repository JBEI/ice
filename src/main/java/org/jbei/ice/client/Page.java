package org.jbei.ice.client;

/**
 * Represents the major pages of the application
 * 
 * @author Hector Plahar
 */
public enum Page {
    LOGIN("login"), MAIN("main"), COLLECTIONS("collections"), ADD_ENTRY("add"), BULK_IMPORT("bulk"), ENTRY_VIEW(
            "entry"), PROFILE("profile"), FEEDBACK("feedback"), ADMIN("admin"), QUERY("query"), BLAST(
            "blast"), STORAGE("storage"), DEBUG("debug"), LOGOUT("logout"), NEWS("news");

    private String token;

    Page(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public static Page tokenToEnum(String token) {
        if (token == null || token.isEmpty())
            return null;

        for (Page page : values()) {
            if (page.getToken().equals(token))
                return page;
        }
        return null;
    }

    public String getLink() {
        return ("page=" + token);
    }
}
