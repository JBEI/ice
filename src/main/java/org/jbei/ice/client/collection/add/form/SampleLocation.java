package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.HashMap;

public class SampleLocation {

    private final HashMap<String, ArrayList<String>> sampleLocation;

    public SampleLocation(HashMap<String, ArrayList<String>> sampleLocation) {
        this.sampleLocation = sampleLocation;
    }

    public HashMap<String, ArrayList<String>> getSampleLocation() {
        return sampleLocation;
    }

    public ArrayList<String> getLocations() {
        return new ArrayList<String>(this.sampleLocation.keySet());
    }

    public ArrayList<String> getListForLocation(String location) {
        return sampleLocation.get(location);
    }
}
