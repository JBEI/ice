package org.jbei.ice.servlet.action;

import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.servlet.executor.Executor;

/**
 * Base action class for communication with the system
 *
 * @author Hector Plahar
 */
public abstract class Action<T extends IDataTransferModel> {

    private String action;
    private String entity;
    private String userId;
    private Paging paging;
    private T params;

    public Action() {
    }

    /**
     * @return operation to be performed
     */
    public String getAction() {
        return action;
    }

    public String getEntity() {
        return entity;
    }

    public abstract Executor getExecutor();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public T getParams() {
        return params;
    }

    public void setParams(T params) {
        this.params = params;
    }
}
