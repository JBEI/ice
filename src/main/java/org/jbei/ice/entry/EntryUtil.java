package org.jbei.ice.entry;

import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.storage.model.SelectionMarker;
import org.jbei.ice.utils.Utils;

import java.util.ArrayList;
import java.util.Set;

/**
 * Utility class for operating on entries
 *
 * @author Hector Plahar
 */
public class EntryUtil {

    public static String getPartNumberPrefix() {
        return Utils.getConfigValue(ConfigurationKey.PART_NUMBER_PREFIX) +
            Utils.getConfigValue(ConfigurationKey.PART_NUMBER_DELIMITER);
    }

    public static ArrayList<String> getSelectionMarkersAsList(Set<SelectionMarker> markers) {
        ArrayList<String> selectionMarkers = new ArrayList<>();
        if (markers == null)
            return selectionMarkers;

        for (SelectionMarker marker : markers) {
            selectionMarkers.add(marker.getName());
        }
        return selectionMarkers;
    }
}
