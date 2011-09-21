package org.jbei.ice.shared.dto;

import java.io.Serializable;

public class StorageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String display;
    private long id;
    private String type;
    private int childCount;

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public StorageInfo() {
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
}
