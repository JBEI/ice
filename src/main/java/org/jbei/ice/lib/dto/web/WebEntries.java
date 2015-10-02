package org.jbei.ice.lib.dto.web;

import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper around a list of entries that are received from other registries on the web
 *
 * @author Hector Plahar
 */
public class WebEntries implements IDataTransferModel {

    private long count;
    private LinkedList<PartData> entries;
    private RegistryPartner registryPartner;

    public WebEntries() {
        this.entries = new LinkedList<>();
    }

    public LinkedList<PartData> getEntries() {
        return this.entries;
    }

    public void setEntries(List<PartData> results) {
        this.entries = new LinkedList<>(results);
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return this.count;
    }

    public void setRegistryPartner(RegistryPartner partner) {
        this.registryPartner = partner;
    }
}
