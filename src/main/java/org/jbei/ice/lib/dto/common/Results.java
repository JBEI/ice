package org.jbei.ice.lib.dto.common;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper around a list of data transfer models in response to a paging request
 *
 * @author Hector Plahar
 */
public class Results<T extends IDataTransferModel> implements IDataTransferModel {

    private long resultCount;
    private List<T> data;

    public Results() {
        this.data = new LinkedList<>();
    }

    public long getResultCount() {
        return resultCount;
    }

    public void setResultCount(long resultCount) {
        this.resultCount = resultCount;
    }

    /**
     * @return list of data models from a retrieve call
     */
    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
