package org.jbei.ice.lib.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AssemblyTable implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<AssemblyBin> items = new ArrayList<AssemblyBin>();

    public AssemblyTable() {
        super();
    }

    public AssemblyTable(List<AssemblyBin> items) {
        super();
        this.items = items;
    }

    public List<AssemblyBin> getItems() {
        return items;
    }

    public void setItems(List<AssemblyBin> items) {
        this.items = items;
    }
}