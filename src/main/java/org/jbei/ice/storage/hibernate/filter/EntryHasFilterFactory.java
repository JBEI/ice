package org.jbei.ice.storage.hibernate.filter;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.filter.impl.CachingWrapperFilter;

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
    public Filter getFilter() {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        for (String f : field) {
            builder.add(new TermQuery(new Term(f, "true")), BooleanClause.Occur.MUST);
        }
        return new CachingWrapperFilter(new QueryWrapperFilter(builder.build()));
    }
}
