package org.jbei.ice.lib.dto.common;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.LinkedList;

/**
 * @author Hector Plahar
 */
public class Results<T extends IDataTransferModel> implements IDataTransferModel {

    private long resultCount;
    private LinkedList<T> data;

    public Results() {
        this.data = new LinkedList<>();
    }

    public long getResultCount() {
        return resultCount;
    }

    public void setResultCount(long resultCount) {
        this.resultCount = resultCount;
    }

    public LinkedList<T> getData() {
        return data;
    }

    public void setData(LinkedList<T> data) {
        this.data = data;
    }
}
