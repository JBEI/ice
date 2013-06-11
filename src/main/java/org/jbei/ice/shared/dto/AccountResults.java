package org.jbei.ice.shared.dto;

import java.util.LinkedList;

/**
 * Wrapper around a list of account infos
 *
 * @author Hector Plahar
 */
public class AccountResults implements IDTOModel {

    private long resultCount;
    private LinkedList<AccountInfo> infos;

    public AccountResults() {
        infos = new LinkedList<AccountInfo>();
    }

    public void setResults(LinkedList<AccountInfo> results) {
        this.infos.clear();
        if (results == null)
            return;

        this.infos.addAll(results);
    }

    public void setResultCount(long count) {
        this.resultCount = count;
    }

    /**
     * @return total query result count. not just the count of results returned
     */
    public long getResultCount() {
        return this.resultCount;
    }

    public LinkedList<AccountInfo> getResults() {
        return this.infos;
    }
}

