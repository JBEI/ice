package org.jbei.ice.client.profile.widget;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * @author Hector Plahar
 */
public enum UserOption {
    PROFILE(FAIconType.USER, "Profile", true, "profile"),
    PREFERENCES(FAIconType.COG, "Preferences", true, "prefs"),
    GROUPS(FAIconType.GROUP, "Groups", true, "groups"),
    MESSAGES(FAIconType.ENVELOPE_ALT, "Messages", false, "msgs"),
    ENTRIES(FAIconType.ALIGN_JUSTIFY, "Entries", false, "entries");

    private final String display;
    private final FAIconType icon;
    private final boolean hasquickAccess;
    private final String url;

    UserOption(FAIconType icon, String display, boolean hasquickAccess, String url) {
        this.display = display;
        this.icon = icon;
        this.hasquickAccess = hasquickAccess;
        this.url = url;
    }

    public String toString() {
        return this.display;
    }

    public FAIconType getIcon() {
        return this.icon;
    }

    public static List<UserOption> quickAccessList() {
        ArrayList<UserOption> list = new ArrayList<UserOption>();
        for (UserOption option : UserOption.values()) {
            if (option.hasquickAccess)
                list.add(option);
        }
        return list;
    }

    public String getUrl() {
        return this.url;
    }
}
