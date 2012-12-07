package org.jbei.ice.client.profile;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * Menu options for the user profile page
 *
 * @author Hector Plahar
 */
public enum UserOption {

    PROFILE(FAIconType.USER, "Profile"),
    PREFERENCES(FAIconType.COG, "Preferences"),
    GROUPS(FAIconType.GROUP, "Private Groups"),
    //    MESSAGES(FAIconType.ENVELOPE_ALT, "Messages"),
    ENTRIES(FAIconType.ALIGN_JUSTIFY, "Entries");

    private final String display;
    private final FAIconType icon;

    UserOption(FAIconType icon, String display) {
        this.display = display;
        this.icon = icon;
    }

    public String toString() {
        return this.display;
    }

    public FAIconType getIcon() {
        return this.icon;
    }

    public static UserOption urlToOption(String selection) {
        if (selection == null || selection.isEmpty())
            return UserOption.PROFILE;

        try {
            return UserOption.valueOf(selection.toUpperCase());
        } catch (Exception npe) {
            return UserOption.PROFILE;
        }
    }

    public String getUrl() {
        return name().toLowerCase();
    }
}
