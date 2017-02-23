package org.jbei.ice.storage.hibernate.filter;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.annotations.Factory;

import java.util.ArrayList;

/**
 * @author Hector Plahar
 */
public class EntryHasFilterFactory {

    private String[] field;

    // injected
    public void setField(ArrayList<String> field) {
        this.field = field.toArray(new String[field.size()]);
    }

    @Factory
    public Query getFilter() {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        for (String f : field) {
            builder.add(new TermQuery(new Term(f, "true")), BooleanClause.Occur.MUST);
        }
        return builder.build();
    }
}
