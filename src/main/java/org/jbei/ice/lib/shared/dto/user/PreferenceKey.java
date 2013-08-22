package org.jbei.ice.lib.shared.dto.user;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Preset key for user preferences. Those that map to {@link org.jbei.ice.lib.shared.dto.bulkupload.EntryField}
 * should have the same toString().ignoreCase() value when the "_" is replaced with a key. e.g.
 * PRINCIPAL_INVESTIGATOR -> Principal Investigator.
 * <p/>
 * To put it another way <pre>Header.stringToHeader(key.toString)</pre> should not return null if
 * the key is for an entry field (e.g. PI)
 *
 * @author Hector Plahar
 */
public enum PreferenceKey implements IDTOModel {

    // entry settings
    PRINCIPAL_INVESTIGATOR(true),
    FUNDING_SOURCE(true);

    private boolean editable;

    PreferenceKey() {
    }

    PreferenceKey(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }

    @Override
    public String toString() {
        return name().replaceAll("_", " ");
    }

    public static PreferenceKey fromString(String key) {
        for (PreferenceKey preferenceKey : PreferenceKey.values()) {
            if (preferenceKey.name().equalsIgnoreCase(key))
                return preferenceKey;
        }
        return null;
    }
}
