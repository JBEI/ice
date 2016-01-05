package org.jbei.ice.storage.hibernate.filter;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.hibernate.search.annotations.Factory;

import java.util.HashSet;

/**
 * @author Hector Plahar
 */
public class EntrySecurityFilterFactory {

    private String accountId;
    private HashSet<String> groupUUids;

    // injected
    public void setAccount(String accountId) {
        this.accountId = accountId;
    }

    // injected
    public void setGroupUUids(HashSet<String> groupUUids) {
        this.groupUUids = groupUUids;
    }

    @Factory
    public Filter getFilter() {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        // must have either account id present or group uuid present
        if (accountId != null) {
            builder.add(new TermQuery(new Term("canRead", accountId)), BooleanClause.Occur.SHOULD);
        }

        if (this.groupUUids != null) {
            for (String uuid : this.groupUUids) {
                builder.add(new TermQuery(new Term("canRead", uuid)), BooleanClause.Occur.SHOULD);
            }
        }

        return new QueryWrapperFilter(builder.build());
    }
}
