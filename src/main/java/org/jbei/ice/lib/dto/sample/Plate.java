package org.jbei.ice.lib.dto.sample;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.HashMap;
import java.util.Map;

public class Plate implements IDataTransferModel {

    private String name;
    private Map<String, Tube> locationBarcodes;
    private boolean hasUserSpecifiedPartIds;

    public boolean isHasUserSpecifiedPartIds() {
        return hasUserSpecifiedPartIds;
    }

    public void setHasUserSpecifiedPartIds(boolean hasUserSpecifiedPartIds) {
        this.hasUserSpecifiedPartIds = hasUserSpecifiedPartIds;
    }

    public Plate() {
        this.locationBarcodes = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Tube> getLocationBarcodes() {
        return locationBarcodes;
    }
}
