package org.jbei.ice.storage.hibernate.filter;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.annotations.Factory;
import org.jbei.ice.lib.access.IndexField;

import java.util.HashSet;

/**
 * @author Hector Plahar
 */
public class EntrySecurityFilterFactory {

    private String accountId;
    private HashSet<String> groupUUids;
    private HashSet<String> folderIds;

    // injected parameter
    public void setAccount(String accountId) {
        this.accountId = accountId;
    }

    // injected parameter
    public void setGroupUUids(HashSet<String> groupUUids) {
        this.groupUUids = groupUUids;
    }

    // injected parameter
    public void setFolderIds(HashSet<String> folderIds) {
        this.folderIds = folderIds;
    }

    @Factory
    public Query getFilter() {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        // must have either account id present or group uuid present
        if (accountId != null) {
            builder.add(new TermQuery(new Term(IndexField.CAN_READ, accountId)), BooleanClause.Occur.SHOULD);
        }

        if (this.groupUUids != null) {
            for (String uuid : this.groupUUids) {
                builder.add(new TermQuery(new Term(IndexField.CAN_READ, uuid)), BooleanClause.Occur.SHOULD);
            }
        }

        if (this.folderIds != null) {
            for (String uuid : this.folderIds) {
                builder.add(new TermQuery(new Term(IndexField.CONTAINED_IN, uuid)), BooleanClause.Occur.SHOULD);
            }
        }

        return builder.build();
    }
}
