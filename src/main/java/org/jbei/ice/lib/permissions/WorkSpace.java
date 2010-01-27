package org.jbei.ice.lib.permissions;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WorkSpace extends LinkedHashMap<String, WorkSpaceItem> {

    private static final long serialVersionUID = 1L;

    public ArrayList<WorkSpaceItem> toArrayList() {
        return new ArrayList<WorkSpaceItem>(this.values());

    }
}
