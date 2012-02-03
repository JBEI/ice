package org.jbei.ice.client.bulkimport.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.dto.EntryInfo;

public abstract class SheetModel {

    public abstract ArrayList<EntryInfo> createInfo(HashMap<Integer, ArrayList<String>> data);

}
