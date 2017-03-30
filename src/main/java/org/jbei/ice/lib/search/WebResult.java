package org.jbei.ice.lib.search;

import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class WebResult implements IDataTransferModel {

    private RegistryPartner partner;
    private long count;
    private List<SearchResult> results;

    public WebResult() {
        this.results = new LinkedList<>();
    }

    public RegistryPartner getPartner() {
        return partner;
    }

    public void setPartner(RegistryPartner partner) {
        this.partner = partner;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }
}
