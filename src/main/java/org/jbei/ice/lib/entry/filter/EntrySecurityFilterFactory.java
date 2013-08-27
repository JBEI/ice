package org.jbei.ice.lib.entry.filter;

import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

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

    @Key
    public FilterKey getKey() {
        StandardFilterKey key = new StandardFilterKey();
        key.addParameter(accountId);
        return key;
    }

    @Factory
    public Filter getFilter() {
        Filter filter = new SecurityFilter(accountId, groupUUids);
        return filter;
    }

    public static class SecurityFilter extends Filter {
        private String accountId;
        private HashSet<String> uuids;

        public SecurityFilter(String id, HashSet<String> uuids) {
            this.accountId = id;
            this.uuids = uuids;
        }

        @Override
        public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
            OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
            TermDocs docs = reader.termDocs(new Term("canRead", accountId));
            while (docs.next()) {
                bitSet.set(docs.doc());
            }

            if (uuids != null) {
                for (String uuid : uuids) {
                    docs = reader.termDocs(new Term("canRead", uuid));
                    while (docs.next()) {
                        bitSet.set(docs.doc());
                    }
                }
            }

            return bitSet;
        }
    }
}
