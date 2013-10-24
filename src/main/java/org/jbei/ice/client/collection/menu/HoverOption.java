package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * {@link HoverCell} mouseover option
 *
 * @author Hector Plahar
 */
public enum HoverOption {

    EDIT(FAIconType.EDIT, "Rename"),
    DELETE(FAIconType.TRASH, "Delete"),
    SHARE(FAIconType.SHARE, "Share"),
    PIN(FAIconType.THUMB_TACK, "Promote"),
    UNPIN(FAIconType.TIMES, "Demote");

    private final String display;
    private final FAIconType icon;

    HoverOption(FAIconType icon, String display) {
        this.display = display;
        this.icon = icon;
    }

    public String toString() {
        return this.display;
    }

    public FAIconType getIcon() {
        return this.icon;
    }
}
