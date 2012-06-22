package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.shared.dto.EntryInfo;

import java.util.ArrayList;

// cell data to entry info
public abstract class SheetModel {

    public abstract void createInfo(ArrayList<SheetFieldData[]> data, ArrayList<EntryInfo> entryList);

}
