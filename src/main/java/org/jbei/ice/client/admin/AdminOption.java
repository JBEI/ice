package org.jbei.ice.client.admin;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * @author Hector Plahar
 */
public enum AdminOption {

    SETTINGS(FAIconType.COGS, "System Settings", "settings"),
    USERS(FAIconType.USER, "Manage Users", "users"),
    GROUPS(FAIconType.GROUP, "Manage Groups", "groups"),
    IMPORT_EXPORT(FAIconType.INBOX, "Transfer Entries", "transfer");

    private final String display;
    private final FAIconType iconType;
    private final String url;

    AdminOption(FAIconType icon, String display, String url) {
        this.iconType = icon;
        this.display = display;
        this.url = url;
    }

    public String toString() {
        return this.display;
    }

    public FAIconType getIcon() {
        return this.iconType;
    }

    public String getUrl() {
        return this.url;
    }
}
