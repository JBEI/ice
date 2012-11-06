package org.jbei.ice.client.entry.view.model;

import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A wrapper for sample info with a bunch of storage data
 * <p/>
 * Backend model of using a storage hierarchy makes things difficult to work with
 *
 * @author Hector Plahar
 */
public class SampleStorage implements IsSerializable {

    private SampleInfo sample;
    private LinkedList<StorageInfo> storageList = new LinkedList<StorageInfo>();

    public SampleStorage() {
    }

    public SampleStorage(SampleInfo sample, List<StorageInfo> storage) {
        this.sample = sample;
        if (storage != null)
            this.storageList.addAll(storage);
    }

    public SampleInfo getSample() {
        return sample;
    }

    public void setSample(SampleInfo info) {
        this.sample = info;
    }

    public LinkedList<StorageInfo> getStorageList() {
        return storageList;
    }
}
