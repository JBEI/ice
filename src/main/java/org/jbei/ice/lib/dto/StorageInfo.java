package org.jbei.ice.lib.dto;

import java.util.ArrayList;

import org.jbei.ice.lib.dao.IDataTransferModel;

public class StorageInfo implements IDataTransferModel {

    private String display;
    private long id;
    private String type;
    private int childCount;
    private ArrayList<PartSample> partSamples;

    public StorageInfo() {
    }

    public StorageInfo(String display) {
        this.display = display;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPartSamples(ArrayList<PartSample> partSamples) {
        this.partSamples = partSamples;
    }

    public ArrayList<PartSample> getPartSamples() {
        return this.partSamples;
    }
}
