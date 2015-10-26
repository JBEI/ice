package org.jbei.ice.lib.dto.common;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class Results<T> implements IDataTransferModel {

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

    public List<T> getData() {
        return data;
    }

    public void setData(LinkedList<T> data) {
        this.data = data;
    }
}
