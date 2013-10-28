package org.jbei.ice.client.entry.display.detail;

import org.jbei.ice.client.common.widget.FAIconType;

/**
 * Upload options for sequence file upload. Currently supports
 * sequence upload via file upload or raw sequence past
 *
 * @author Hector Plahar
 */
public enum UploadOption {

    FILE(FAIconType.UPLOAD, "File Upload", "file"),
    PASTE(FAIconType.PASTE, "Paste Sequence", "paste");

    private String display;
    private String type;
    private FAIconType iconType;

    private UploadOption(FAIconType iconType, String display, String type) {
        this.iconType = iconType;
        this.display = display;
        this.type = type;
    }

    public FAIconType getIconType() {
        return this.iconType;
    }

    @Override
    public String toString() {
        return this.display;
    }

    public String getType() {
        return this.type;
    }
}
