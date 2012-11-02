package org.jbei.ice.client.admin;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * @author Hector Plahar
 */
public enum AdminOption {

    SETTINGS(FAIconType.COGS, "System Settings", "settings"),
    USERS(FAIconType.USER, "Manage Users", "users"),
    GROUPS(FAIconType.GROUP, "Manage Groups", "Create/Edit/Delete/Update groups and organize members"),
    TRANSFER(FAIconType.INBOX, "Transfer Entries", "transfer entries between ICE instances");

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
        if (url == null)
            return null;

        for (AdminOption option : AdminOption.values()) {
            if (url.trim().equalsIgnoreCase(option.getUrl()))
                return option;
        }
        return null;
    }

    public String getDescription() {
        return description;
    }
}
