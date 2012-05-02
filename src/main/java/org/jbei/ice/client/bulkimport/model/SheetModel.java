package org.jbei.ice.client.bulkimport.model;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.EntryInfo;

// cell data to entry info
public abstract class SheetModel {

    public abstract void createInfo(ArrayList<SheetFieldData[]> data,
            ArrayList<EntryInfo> primaryData, ArrayList<EntryInfo> secondaryData);

}
