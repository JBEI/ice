package org.jbei.ice.lib.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AssemblyBin implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<AssemblyItem> items = new ArrayList<AssemblyItem>();
    private String type = "";

    public AssemblyBin() {
        super();
    }

    public AssemblyBin(List<AssemblyItem> items) {
        super();

        this.items = items;
    }

    public List<AssemblyItem> getItems() {
        return items;
    }

    public void setItems(List<AssemblyItem> items) {
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}