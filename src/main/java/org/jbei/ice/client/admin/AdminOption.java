package org.jbei.ice.client.admin;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * @author Hector Plahar
 */
public enum AdminOption {

    SETTINGS(FAIconType.COGS, "System Settings", "settings"),
    WEB(FAIconType.GLOBE, "Web of Registries", "Web of registries settings"),
    USERS(FAIconType.USER, "Manage Users", "users"),
    GROUPS(FAIconType.GROUP, "Manage Groups", "Create/Edit/Delete/Update groups and organize members"),
    SEARCH(FAIconType.SEARCH, "Manage Search", "Rebuild the search indices and change search settings");
//    TRANSFER(FAIconType.INBOX, "Transfer Entries", "transfer entries between ICE instances");

    private final String display;
    private final FAIconType iconType;
    private final String description;

    AdminOption(FAIconType icon, String display, String description) {
        this.iconType = icon;
        this.display = display;
        this.description = description;
    }

    public String toString() {
        return this.display;
    }

    public FAIconType getIcon() {
        return this.iconType;
    }

    public String getUrl() {
        return this.name().toLowerCase();
    }

    public static AdminOption urlToOption(String url) {
        if (url == null || url.isEmpty())
            return AdminOption.SETTINGS;

        try {
            return AdminOption.valueOf(url.toUpperCase());
        } catch (Exception e) {
            return AdminOption.SETTINGS;
        }
    }

    public String getDescription() {
        return description;
    }
}

