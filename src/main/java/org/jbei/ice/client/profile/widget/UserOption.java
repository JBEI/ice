package org.jbei.ice.client.profile.widget;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * @author Hector Plahar
 */
public enum UserOption {
    PROFILE(FAIconType.USER, "Profile", "profile"),
    PREFERENCES(FAIconType.COG, "Preferences", "prefs"),
    GROUPS(FAIconType.GROUP, "Groups", "groups"),
    MESSAGES(FAIconType.ENVELOPE_ALT, "Messages", "msgs"),
    ENTRIES(FAIconType.ALIGN_JUSTIFY, "Entries", "entries");

    private final String display;
    private final FAIconType icon;
    private final String url;

    UserOption(FAIconType icon, String display, String url) {
        this.display = display;
        this.icon = icon;
        this.url = url;
    }

    public String toString() {
        return this.display;
    }

    public FAIconType getIcon() {
        return this.icon;
    }

    public String getUrl() {
        return this.url;
    }
}
