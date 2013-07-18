package org.jbei.ice.lib.shared.dto;

import java.util.LinkedList;

import org.jbei.ice.lib.shared.dto.user.User;

/**
 * Wrapper around a list of user accounts
 *
 * @author Hector Plahar
 */
public class AccountResults implements IDTOModel {

    private long resultCount;
    private LinkedList<User> infos;

    public AccountResults() {
        infos = new LinkedList<User>();
    }

    public void setResults(LinkedList<User> results) {
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

    public LinkedList<User> getResults() {
        return this.infos;
    }
}

