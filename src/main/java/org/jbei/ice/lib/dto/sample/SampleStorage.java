package org.jbei.ice.lib.dto.sample;

import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.lib.dto.PartSample;
import org.jbei.ice.lib.dto.StorageInfo;

/**
 * A wrapper for partSample info with a bunch of storage data
 * <p/>
 * Backend model of using a storage hierarchy makes things difficult to work with
 *
 * @author Hector Plahar
 */
public class SampleStorage {

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
