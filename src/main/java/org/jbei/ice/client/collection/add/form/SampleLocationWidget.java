package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

// TODO : this probably does not need to be a widget
public class SampleLocationWidget implements IsWidget {

    private final HashMap<String, ArrayList<String>> sampleLocation;

    public SampleLocationWidget(HashMap<String, ArrayList<String>> sampleLocation) {
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

    @Override
    public Widget asWidget() {
        // TODO Auto-generated method stub
        return null;
    }
}
