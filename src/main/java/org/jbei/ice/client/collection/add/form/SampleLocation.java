package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.jbei.ice.lib.shared.dto.PartSample;

public class SampleLocation {

    private final HashMap<PartSample, ArrayList<String>> sampleLocation;

    public SampleLocation(HashMap<PartSample, ArrayList<String>> sampleLocation) {
        this.sampleLocation = sampleLocation;
    }

    public ArrayList<PartSample> getLocations() {
        ArrayList<PartSample> locations = new ArrayList<PartSample>(sampleLocation.keySet());
        Collections.sort(locations);
        return locations;
    }

    public ArrayList<String> getListForLocation(String locationId) {
        ArrayList<String> list = new ArrayList<String>();

        for (PartSample part : sampleLocation.keySet()) {
            if (locationId.equalsIgnoreCase(part.getLocationId())) {
                list.addAll(sampleLocation.get(part));
                break;
            }
        }

        return list;
    }
}
