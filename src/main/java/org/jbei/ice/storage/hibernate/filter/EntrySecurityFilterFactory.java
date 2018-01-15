package org.jbei.ice.storage.hibernate.filter;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.filter.impl.CachingWrapperFilter;

import java.util.Set;

/**
 * @author Hector Plahar
 * @author William Morrell
 */
public class EntrySecurityFilterFactory {

    private String accountId;
    private Set<String> groupUUids;

    // injected parameter
    public void setAccount(String accountId) {
        this.accountId = accountId;
    }

    // injected parameter
    public void setGroupUUids(Set<String> groupUUids) {
        this.groupUUids = groupUUids;
    }

    @Factory
    public Filter getFilter() {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        // must have either account id present or group uuid present
        if (accountId != null) {
            Term accountTerm = new Term("canRead_" + accountId, accountId);
            builder.add(new TermQuery(accountTerm), BooleanClause.Occur.SHOULD);
        }

        if (this.groupUUids != null) {
            for (String uuid : this.groupUUids) {
                Term groupTerm = new Term("canRead_" + uuid, uuid);
                builder.add(new TermQuery(groupTerm), BooleanClause.Occur.SHOULD);
            }
        }

        return new CachingWrapperFilter(new QueryWrapperFilter(builder.build()));
    }
}
