package org.jbei.ice.dto.user;

import org.jbei.ice.dto.entry.EntryFieldLabel;

/**
 * Preset key for user preferences. Those that map to {@link EntryFieldLabel}
 * should have the same toString().ignoreCase() value when the "_" is replaced with a key. e.g.
 * PRINCIPAL_INVESTIGATOR -> Principal Investigator.
 * <p/>
 * To put it another way <pre>Header.stringToHeader(key.toString)</pre> should not return null if
 * the key is for an entry field (e.g. PI)
 *
 * @author Hector Plahar
 */
public enum PreferenceKey {

    // entry settings
    PRINCIPAL_INVESTIGATOR(),
    FUNDING_SOURCE();

    PreferenceKey() {
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
