package org.jbei.ice.client.admin;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * Menu options for the administrative page
 *
 * @author Hector Plahar
 */
public enum AdminOption {

    SETTINGS(FAIconType.COGS, "System Settings", "Site wide system settings"),
    WEB(FAIconType.GLOBE, "Web of Registries",
        "Enable/Disable information sharing and retrieval from other ICE instances"),
    USERS(FAIconType.USER, "Manage Users", "Manage user accounts and privileges"),
    GROUPS(FAIconType.GROUP, "Manage Groups", "Create/Edit/Delete/Update groups and organize members"),
    PARTS(FAIconType.LIST, "Transferred Parts", "Approve/Reject parts transferred from other registries");

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

