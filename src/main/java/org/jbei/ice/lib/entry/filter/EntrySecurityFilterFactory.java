package org.jbei.ice.lib.entry.filter;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.OpenBitSet;
import org.hibernate.search.annotations.Factory;

import java.io.IOException;
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
        public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
            OpenBitSet bitSet = new OpenBitSet(context.reader().maxDoc());
            DocsEnum docs;

            if (accountId != null) {
                docs = context.reader().termDocsEnum(new Term("canRead", accountId));
                if (docs != null) {
                    int doc;
                    while ((doc = docs.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                        bitSet.set(doc);
                    }
                }
            }

            if (uuids != null) {
                for (String uuid : uuids) {
                    docs = context.reader().termDocsEnum(new Term("canRead", uuid));
                    if (docs != null) {
                        int doc;
                        while ((doc = docs.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                            bitSet.set(doc);
                        }
                    }
                }
            }

            return bitSet;
        }
    }
}
