package org.jbei.ice.client.entry.display.model;

import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.StorageInfo;

/**
 * A wrapper for partSample info with a bunch of storage data
 * <p/>
 * Backend model of using a storage hierarchy makes things difficult to work with
 *
 * @author Hector Plahar
 */
public class SampleStorage implements IDTOModel {

    private PartSample partSample;
    private LinkedList<StorageInfo> storageList = new LinkedList<StorageInfo>();

    public SampleStorage() {
    }

    public SampleStorage(PartSample partSample, List<StorageInfo> storage) {
        this.partSample = partSample;
        if (storage != null)
            this.storageList.addAll(storage);
    }

    public PartSample getPartSample() {
        return partSample;
    }

    public void setPartSample(PartSample part) {
        this.partSample = part;
    }

    public LinkedList<StorageInfo> getStorageList() {
        return storageList;
    }
}
