package org.jbei.ice.lib.entry.filter;

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
        BooleanQuery query = new BooleanQuery(true);
        for (String f : field) {
            query.add(new TermQuery(new Term(f, "true")), BooleanClause.Occur.MUST);
        }
        return new CachingWrapperFilter(new QueryWrapperFilter(query));
    }
}
