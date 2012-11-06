package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.jbei.ice.shared.dto.SampleInfo;

public class SampleLocation {

    private final HashMap<SampleInfo, ArrayList<String>> sampleLocation;

    public SampleLocation(HashMap<SampleInfo, ArrayList<String>> sampleLocation) {
        this.sampleLocation = sampleLocation;
    }

    public ArrayList<SampleInfo> getLocations() {
        ArrayList<SampleInfo> locations = new ArrayList<SampleInfo>(sampleLocation.keySet());
        Collections.sort(locations);
        return locations;
    }

    public ArrayList<String> getListForLocation(String locationId) {
        ArrayList<String> list = new ArrayList<String>();

        for (SampleInfo info : sampleLocation.keySet()) {
            if (locationId.equalsIgnoreCase(info.getLocationId())) {
                list.addAll(sampleLocation.get(info));
                break;
            }
        }

        return list;
    }
}
