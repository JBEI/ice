package org.jbei.ice.lib.vo;

import java.util.ArrayList;
import java.util.List;

public class Permutation {
    private List<AssemblyItem> items = new ArrayList<AssemblyItem>();

    public Permutation() {
    }

    public List<AssemblyItem> getItems() {
        return items;
    }

    public void setItems(List<AssemblyItem> value) {
        items = value;
    }

    public void addAssemblyItem(AssemblyItem item) {
        items.add(item);
    }

    public Permutation clone() {
        Permutation clonedPermutation = new Permutation();

        if (items.size() > 0) {
            for (int i = 0; i < items.size(); i++) {
                clonedPermutation.addAssemblyItem(items.get(i));
            }
        }

        return clonedPermutation;
    }
}
