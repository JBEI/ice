package org.jbei.ice.client.profile.widget;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * @author Hector Plahar
 */
public enum UserOption {
    PROFILE(FAIconType.USER, "Profile", true),
    PREFERENCES(FAIconType.COG, "Preferences", true),
    GROUPS(FAIconType.GROUP, "Groups", true),
    MESSAGES(FAIconType.ENVELOPE_ALT, "Messages", false),
    ENTRIES(FAIconType.ALIGN_JUSTIFY, "Entries", false);

    private final String display;
    private final FAIconType icon;
    private final boolean hasquickAccess;

    UserOption(FAIconType icon, String display, boolean hasquickAccess) {
        this.display = display;
        this.icon = icon;
        this.hasquickAccess = hasquickAccess;
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
}
