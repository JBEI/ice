package org.jbei.ice.shared.dto;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class StorageInfo implements IsSerializable {

    private String display;
    private long id;
    private String type;
    private int childCount;
    private ArrayList<SampleInfo> samples;

    public StorageInfo() {
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

    public void setSamples(ArrayList<SampleInfo> samples) {
        this.samples = samples;
    }

    public ArrayList<SampleInfo> getSamples() {
        return this.samples;
    }
}
